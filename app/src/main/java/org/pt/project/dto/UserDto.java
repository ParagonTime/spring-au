package org.pt.project.dto;

import lombok.Data;
import org.pt.project.entity.Role;

import java.time.Instant;
import java.util.UUID;

@Data
public class UserDto {
    private Long id;
    private UUID userId;
    private String login;
    private Role role;
    private String email;
    private Instant createdAt;
}
