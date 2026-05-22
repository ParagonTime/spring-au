package org.pt.project.event;

import org.pt.project.entity.TaskStatus;

public record TaskStatusChangedFlowEvent(Long taskId, TaskStatus status, String createdAt) {
}
