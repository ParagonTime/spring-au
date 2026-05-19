package org.pt.project.event;

import org.pt.project.entity.Role;

import java.time.Instant;


public record UserStreamEvent(Long id, Role role, String email, String createdAt) {
}
