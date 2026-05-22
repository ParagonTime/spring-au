package org.pt.project.event;

import org.pt.project.entity.Role;

import java.util.UUID;

public record UserStreamEvent(UUID userId, Role role, String email, String createdAt) {
}
