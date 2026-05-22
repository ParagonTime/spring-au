package org.pt.project.event;

import java.util.UUID;

public record UserUpdatedFlowEvent(UUID userId, String createdAt) {
}
