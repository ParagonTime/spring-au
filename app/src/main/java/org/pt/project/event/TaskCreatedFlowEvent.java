package org.pt.project.event;

public record TaskCreatedFlowEvent(Long taskId, String createdAt) {
}
