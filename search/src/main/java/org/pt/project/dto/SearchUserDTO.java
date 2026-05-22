package org.pt.project.dto;

import org.pt.project.entity.Role;

import java.time.Instant;
import java.util.UUID;

public record SearchUserDTO(UUID userId,
                            Role role,
                            String email,
                            Instant createdAt
) {
}
