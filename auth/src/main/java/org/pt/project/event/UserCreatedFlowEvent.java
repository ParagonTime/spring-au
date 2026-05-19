package org.pt.project.event;

import java.time.Instant;

public record UserCreatedFlowEvent(Long id, String createdAt) {
}
