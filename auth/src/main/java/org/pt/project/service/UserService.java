package org.pt.project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pt.project.dto.UpdateUserRequest;
import org.pt.project.entity.User;
import org.pt.project.event.UserDeletedFlowEvent;
import org.pt.project.event.UserStreamEvent;
import org.pt.project.event.UserUpdatedFlowEvent;
import org.pt.project.exception.NoFoundException;
import org.pt.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final KeycloakService keycloakService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${auth.kafka.topics.user-stream}")
    private String userStreamTopic;

    @Value("${auth.kafka.topics.user-flow}")
    private String userFlowTopic;

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Transactional(readOnly = true)
    public User getUserByUserId(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new NoFoundException("User no found id: " + userId));
    }

    @Transactional
    public User updateUser(String userId, UpdateUserRequest request) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new NoFoundException("User no found id: " + userId));

        int updateCounter = 0;
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            Optional<User> userByEmail = userRepository.findByEmail(request.getEmail()); // нужно ли проверять, когда email уже UNIQUE
            if (userByEmail.isPresent() && !userByEmail.get().getUserId().toString().equals(userId)) {
                throw new AccessDeniedException("Email is used");
            }
            user.setEmail(request.getEmail());
            updateCounter++;
        }
        if (request.getLogin() != null && !request.getLogin().isBlank()) {
            user.setLogin(request.getLogin());
            updateCounter++;
        }
        if (updateCounter > 0) {
            keycloakService.updateKeycloakUser(UUID.fromString(userId), Map.of("email", request.getEmail()));
        }

        User savedUser = userRepository.save(user);

        UserStreamEvent userStreamEvent = new UserStreamEvent(
                savedUser.getUserId(),
                savedUser.getRole(),
                savedUser.getEmail(),
                savedUser.getCreatedAt().toString()
        );
        Instant timeStamp = Instant.now();
        UserUpdatedFlowEvent userUpdatedFlowEvent = new UserUpdatedFlowEvent(
                savedUser.getUserId(),
                timeStamp.toString()
        );

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                kafkaTemplate.send(userFlowTopic, userUpdatedFlowEvent);
                kafkaTemplate.send(userStreamTopic, userStreamEvent);
            }
        });
        return savedUser;
    }

    @Transactional
    public void deleteUser(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new NoFoundException("User no found id: " + userId));

        keycloakService.deleteKeycloakUser(UUID.fromString(userId));
        userRepository.delete(user);

        UserStreamEvent userStreamEvent = new UserStreamEvent(
                user.getUserId(),
                user.getRole(),
                user.getEmail(),
                user.getCreatedAt().toString()
        );
        Instant timeStamp = Instant.now();
        UserDeletedFlowEvent userDeletedFlowEvent = new UserDeletedFlowEvent(
                user.getUserId(),
                timeStamp.toString()
        );

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                kafkaTemplate.send(userFlowTopic, userDeletedFlowEvent);
                kafkaTemplate.send(userStreamTopic, userStreamEvent);
            }
        });
    }
}
