package org.pt.project.service;

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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepository userRepository;
    private final KeycloakService keycloakService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${auth.kafka.topics.user-stream}")
    private String userStreamTopic;

    @Value("${auth.kafka.topics.user-flow}")
    private String userFlowTopic;

    private static final String LOGIN_DUPLICATE_MESSAGE = "Логин уже используется ";

    @Transactional
    public void createUser(UserRegistrationRequest request) {
        if (userRepository.findByLogin(request.getLogin()).isPresent()) {
            log.error(LOGIN_DUPLICATE_MESSAGE + "{}", request.getLogin());
            throw new LoginDuplicateException(LOGIN_DUPLICATE_MESSAGE + request.getLogin());
        }

        UUID keycloakUserId = keycloakService.createKeycloakUser(request);

        User newUser = new User();
        newUser.setLogin(request.getLogin());
        newUser.setEmail(request.getEmail());
        newUser.setRole(Role.USER);
        newUser.setCreatedAt(Instant.now());
        newUser.setUserId(keycloakUserId);
        User savedUser = userRepository.save(newUser);

        UserCreatedFlowEvent userCreatedFlowEvent = new UserCreatedFlowEvent(
                savedUser.getUserId(),
                savedUser.getCreatedAt().toString()
        );

        UserStreamEvent userStreamEvent = new UserStreamEvent(
                savedUser.getUserId(),
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
        log.info("User created: {}", newUser);
    }
}