package org.pt.project.event;

import java.util.UUID;

public record UserCreatedFlowEvent(UUID userId, String createdAt) {
}
