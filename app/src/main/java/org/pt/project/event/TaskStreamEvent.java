package org.pt.project.event;

import org.pt.project.dto.UserDto;
import org.pt.project.entity.TaskStatus;

import java.time.Instant;

public record TaskStreamEvent(Long id, String title, UserDto user, String description, TaskStatus status, String createdAt) {
}