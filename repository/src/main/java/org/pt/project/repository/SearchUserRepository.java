package org.pt.project.repository;

import org.pt.project.entity.SearchUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SearchUserRepository extends JpaRepository<SearchUser, Long> {
    Optional<SearchUser> findByUserId(UUID uuid);

    @Query(value = "SELECT * FROM search_schema.search_users WHERE similarity(email, :email) > 0.3", nativeQuery = true)
    List<SearchUser> findByEmail(@Param("email") String email);
}
