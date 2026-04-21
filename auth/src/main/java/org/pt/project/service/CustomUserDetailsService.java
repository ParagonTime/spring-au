package org.pt.project.service;


import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pt.project.entity.User;
import org.pt.project.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    private static final String USER_NOT_FOUND_MESSAGE = "Пользователь с таким логином не найден  ";

    @Override
    @NonNull
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        User user = userRepository.findByLogin(username).orElseThrow(() ->  {
            log.error(USER_NOT_FOUND_MESSAGE + "{}", username);
            return new UsernameNotFoundException(USER_NOT_FOUND_MESSAGE + username);
        });
        log.info("Запрошен пользователь {}", user.getLogin());
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getLogin())
                .password(user.getPasswordHash())
                .authorities("ROLE_" + user.getRole().name())
                .build();
    }
}
