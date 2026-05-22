package org.pt.project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pt.project.dto.UserRegistrationRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakService {

    @Value("${keycloak.admin.username}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getAdminToken() {
        String tokenUrl = authServerUrl + "/realms/master/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", "admin-cli");
        body.add("grant_type", "password");
        body.add("username", adminUsername);
        body.add("password", adminPassword);

        ResponseEntity<Map> response = restTemplate.exchange(
                tokenUrl, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);

        return (String) response.getBody().get("access_token");
    }

    public UUID createKeycloakUser(UserRegistrationRequest request) {
        String url = authServerUrl + "/admin/realms/" + realm + "/users";

        Map<String, Object> userMap = Map.of(
                "username", request.getLogin(),
                "email", request.getEmail(),
                "enabled", true,
                "firstName", request.getLogin(),
                "lastName", request.getLogin(),
                "credentials", List.of(Map.of(
                        "type", "password",
                        "value", request.getPassword(),
                        "temporary", false
                ))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getAdminToken());

        ResponseEntity<Void> response = restTemplate.exchange(
                url, HttpMethod.POST, new HttpEntity<>(userMap, headers), Void.class);

        URI location = response.getHeaders().getLocation();
        if (location == null) {
            throw new IllegalStateException("Keycloak не вернул Location заголовок");
        }

        String path = location.getPath();

        log.info("Пользователь {} создан в Keycloak", request.getLogin());

        String userIdStr = path.substring(path.lastIndexOf('/') + 1);
        return UUID.fromString(userIdStr);
    }

    public void updateKeycloakUser(UUID userId, Map<String, Object> updates) {
        String url = authServerUrl + "/admin/realms/" + realm + "/users/" + userId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getAdminToken());

        restTemplate.exchange(url, HttpMethod.PUT,
                new HttpEntity<>(updates, headers), Void.class);
    }

    public void deleteKeycloakUser(UUID userId) {
        String url = authServerUrl + "/admin/realms/" + realm + "/users/" + userId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAdminToken());

        restTemplate.exchange(url, HttpMethod.DELETE,
                new HttpEntity<>(headers), Void.class);
    }
}
