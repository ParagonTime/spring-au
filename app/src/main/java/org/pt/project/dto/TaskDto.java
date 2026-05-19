package org.pt.project.dto;

import lombok.Data;
import org.pt.project.entity.TaskStatus;

import java.time.Instant;

@Data
public class TaskDto {
    private Long id;
    private String title;
    private UserDto user;
    private String description;
    private TaskStatus status;
    private Instant createdAt;
}
