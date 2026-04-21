package org.pt.project.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pt.project.dto.UserRegistrationRequest;
import org.pt.project.entity.Role;
import org.pt.project.entity.User;
import org.pt.project.exception.LoginDuplicateException;
import org.pt.project.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder bCryptPasswordEncoder;

    private static final String LOGIN_DUPLICATE_MESSAGE = "Логин уже используется ";

    @Transactional
    public void createUser(UserRegistrationRequest request) {
        if (userRepository.findByLogin(request.getLogin()).isPresent()) {
            log.error(LOGIN_DUPLICATE_MESSAGE + "{}", request.getLogin());
            throw new LoginDuplicateException(LOGIN_DUPLICATE_MESSAGE + request.getLogin());
        }
        String passwordHash = bCryptPasswordEncoder.encode(request.getPassword());
        User newUser = new User();
        newUser.setLogin(request.getLogin());
        newUser.setPasswordHash(passwordHash);
        newUser.setRole(Role.USER);
        userRepository.save(newUser);
        log.info("safe new user: {}", newUser);
    }
}
