package org.pt.project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pt.project.dto.LoginRequest;
import org.pt.project.dto.TokenResponse;
import org.pt.project.exception.InvalidCredentialsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizationService {

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    private final RestTemplate restTemplate = new RestTemplate();

    public TokenResponse getToken(LoginRequest request) {
        String tokenUrl = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("grant_type", "password");
        body.add("username", request.getLogin());
        body.add("password", request.getPassword());
        body.add("scope", "openid");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    tokenUrl, HttpMethod.POST, entity, Map.class);

            Map<String, Object> tokenData = response.getBody();
            if (tokenData == null || tokenData.get("access_token") == null) {
                throw new InvalidCredentialsException("Keycloak не вернул access_token");
            }

            String accessToken = (String) tokenData.get("access_token");
            long expiresIn = ((Number) tokenData.get("expires_in")).longValue();

            log.info("Token issued for user {}", request.getLogin());
            return new TokenResponse(accessToken, Instant.now().plusSeconds(expiresIn));
        } catch (HttpClientErrorException e) {
            log.warn(
                    "Token request failed for user {}: {} {}",
                    request.getLogin(),
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString()
            );
            throw new InvalidCredentialsException("Неверный логин или пароль");
        }
    }
}