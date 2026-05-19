package org.pt.project.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pt.project.dto.UserRegistrationRequest;
import org.pt.project.entity.Role;
import org.pt.project.entity.User;
import org.pt.project.event.UserCreatedFlowEvent;
import org.pt.project.event.UserStreamEvent;
import org.pt.project.exception.LoginDuplicateException;
import org.pt.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepository userRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin.username}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    @Value("${auth.kafka.topics.user-stream}")
    private String userStreamTopic;

    @Value("${auth.kafka.topics.user-flow}")
    private String userFlowTopic;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String LOGIN_DUPLICATE_MESSAGE = "Логин уже используется ";

    @Transactional
    public void createUser(UserRegistrationRequest request) {
        if (userRepository.findByLogin(request.getLogin()).isPresent()) {
            log.error(LOGIN_DUPLICATE_MESSAGE + "{}", request.getLogin());
            throw new LoginDuplicateException(LOGIN_DUPLICATE_MESSAGE + request.getLogin());
        }

        createKeycloakUser(request);

        User newUser = new User();
        newUser.setLogin(request.getLogin());
        newUser.setEmail(request.getEmail());
        newUser.setRole(Role.USER);
        newUser.setCreatedAt(Instant.now());
        User savedUser = userRepository.save(newUser);

        UserCreatedFlowEvent userCreatedFlowEvent = new UserCreatedFlowEvent(
                savedUser.getId(),
                savedUser.getCreatedAt().toString()
        );

        UserStreamEvent userStreamEvent = new UserStreamEvent(
                savedUser.getId(),
                savedUser.getRole(),
                savedUser.getEmail(),
                savedUser.getCreatedAt().toString()
        );

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                kafkaTemplate.send(userFlowTopic, userCreatedFlowEvent);
                kafkaTemplate.send(userStreamTopic, userStreamEvent);
            }
        });
        log.info("Сохранен новый пользователь: {}", newUser);
    }

    private void createKeycloakUser(UserRegistrationRequest request) {
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



        restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(userMap, headers), String.class);
        log.info("Пользователь {} создан в Keycloak", request.getLogin());
    }

    private String getAdminToken() {
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
}