package org.pt.project.dto;

import org.pt.project.entity.TaskStatus;

import java.time.Instant;
import java.util.UUID;

public record SearchTaskDto(Long taskId,
                            String title,
                            String description,
                            TaskStatus status,
                            UUID userId,
                            Instant createdAt
) {}
