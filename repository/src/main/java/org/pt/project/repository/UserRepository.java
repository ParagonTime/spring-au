package org.pt.project.repository;

import org.pt.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findById(Long id);

    boolean existsById(Long id);

    Optional<User> findByLogin(String login);

    Optional<User> findByUserId(String userId);

    Optional<User> findByEmail(String email);
}
