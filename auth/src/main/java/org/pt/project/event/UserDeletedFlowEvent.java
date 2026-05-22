package org.pt.project.event;

import java.util.UUID;

public record UserDeletedFlowEvent(UUID userId, String createdAt) {
}
