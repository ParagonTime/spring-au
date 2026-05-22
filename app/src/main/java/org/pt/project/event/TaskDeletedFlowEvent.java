package org.pt.project.event;

public record TaskDeletedFlowEvent(Long taskId, String deletedAt) {
}
