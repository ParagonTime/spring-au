package org.pt.project.dto;

import lombok.Data;
import org.pt.project.entity.TaskStatus;

import java.time.Instant;
import java.util.UUID;

@Data
public class TaskDto {
    private Long id;
    private String title;
    private UUID userId;
    private String description;
    private TaskStatus status;
    private Instant createdAt;
}
