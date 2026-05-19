package org.pt.project.event;

import java.time.Instant;

public record TaskCreatedFlowEvent(Long id, String createdAt) {
}
