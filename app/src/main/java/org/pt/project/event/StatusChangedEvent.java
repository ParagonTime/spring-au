package org.pt.project.event;

import org.pt.project.entity.TaskStatus;

public record StatusChangedEvent(Long taskId, TaskStatus newStatus) {
}
