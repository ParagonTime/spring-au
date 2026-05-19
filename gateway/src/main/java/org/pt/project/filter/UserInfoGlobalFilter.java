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

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    private final WebClient webClient = WebClient.create();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (path.startsWith("/registration") || path.startsWith("/api/auth")) {
            return chain.filter(exchange);
        }

        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(auth -> (Jwt) auth.getPrincipal())
                .flatMap(jwt -> fetchUserInfo(jwt.getTokenValue()))
                .flatMap(userInfo -> {
                    ServerWebExchange modifiedExchange = exchange.mutate()
                            .request(r -> r.headers(headers -> {
                                headers.set("user-id", String.valueOf(userInfo.get("sub")));
                                Map<String, List<String>> realmAccess =
                                        (Map<String, List<String>>) userInfo.get("realm_access");
                                headers.set("user-roles", String.join(",", realmAccess.get("roles")));
                            }))
                            .build();
                    return chain.filter(modifiedExchange);
                })
                .switchIfEmpty(chain.filter(exchange));
    }

    private Mono<Map> fetchUserInfo(String token) {
        return webClient.get()
                .uri(issuerUri + "/protocol/openid-connect/userinfo")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Map.class);
    }
}