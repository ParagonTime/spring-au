package org.pt.project.repository;

import org.pt.project.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t WHERE t.userId = :id")
    Page<Task> findAllWithUser(Pageable pageable,@Param("id") String userId);

    @Query("SELECT t FROM Task t WHERE t.id = :id")
    Optional<Task> findTaskById(@Param("id") Long id);
}
