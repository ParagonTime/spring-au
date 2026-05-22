package org.pt.project.event;

import org.pt.project.entity.TaskStatus;

import java.util.UUID;

public record TaskStreamEvent(Long taskId, String title, UUID userId, String description, TaskStatus status, String createdAt) {
}
