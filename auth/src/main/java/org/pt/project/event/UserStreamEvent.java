package org.pt.project.event;

import org.pt.project.entity.Role;


public record UserStreamEvent(java.util.UUID id, Role role, String email, String createdAt) {
}
