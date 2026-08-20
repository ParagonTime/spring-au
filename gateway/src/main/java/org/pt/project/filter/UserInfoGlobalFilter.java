package org.pt.project.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class UserInfoGlobalFilter implements GlobalFilter {

    private static final String OBSERVABILITY_PATH =
            "/api/v1/observability";

    private final WebClient webClient = WebClient.create();

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain
    ) {
        String path = exchange.getRequest()
                .getURI()
                .getPath();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .filter(authentication ->
                        authentication.getPrincipal() instanceof Jwt
                )
                .map(authentication ->
                        (Jwt) authentication.getPrincipal()
                )
                .flatMap(jwt ->
                        fetchUserInfo(jwt.getTokenValue())
                )
                .flatMap(userInfo -> {
                    String userId = String.valueOf(
                            userInfo.get("sub")
                    );

                    String rolesString = extractRoles(userInfo);

                    ServerWebExchange modifiedExchange =
                            exchange.mutate()
                                    .request(request -> request.headers(
                                            headers -> {
                                                headers.set(
                                                        "user-id",
                                                        userId
                                                );
                                                headers.set(
                                                        "user-roles",
                                                        rolesString
                                                );
                                            }
                                    ))
                                    .build();

                    return chain.filter(modifiedExchange);
                })
                .switchIfEmpty(chain.filter(exchange));
    }

    private boolean isPublicPath(String path) {
        return path.equals("/registration")
                || path.startsWith("/registration/")
                || path.equals("/api/auth")
                || path.startsWith("/api/auth/")
                || path.equals(OBSERVABILITY_PATH)
                || path.startsWith(OBSERVABILITY_PATH + "/");
    }

    private String extractRoles(Map<String, Object> userInfo) {
        Object realmAccessObject = userInfo.get("realm_access");

        if (!(realmAccessObject instanceof Map<?, ?> realmAccess)) {
            return "";
        }

        Object rolesObject = realmAccess.get("roles");

        if (!(rolesObject instanceof List<?> roles)) {
            return "";
        }

        return roles.stream()
                .map(String::valueOf)
                .reduce((left, right) -> left + "," + right)
                .orElse("");
    }

    private Mono<Map> fetchUserInfo(String token) {
        return webClient.get()
                .uri(issuerUri + "/protocol/openid-connect/userinfo")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Map.class);
    }
}