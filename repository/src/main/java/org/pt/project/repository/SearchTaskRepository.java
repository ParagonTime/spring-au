package org.pt.project.repository;

import org.pt.project.entity.SearchTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SearchTaskRepository extends JpaRepository<SearchTask, Long> {
    Optional<SearchTask> findByTaskId(Long searchTaskId);
}
