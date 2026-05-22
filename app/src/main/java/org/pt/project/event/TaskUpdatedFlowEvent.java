package org.pt.project.event;

public record TaskUpdatedFlowEvent(Long taskId, String updatedAt) {
}
