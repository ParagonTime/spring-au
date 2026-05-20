package org.pt.project.event;

import org.pt.project.dto.UserDto;
import org.pt.project.entity.TaskStatus;

import java.time.Instant;
import java.util.UUID;

public record TaskStreamEvent(Long id, String title, UUID userId, String description, TaskStatus status, String createdAt) {
}