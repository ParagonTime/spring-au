package org.pt.project.dto;

import lombok.Data;
import org.pt.project.entity.Role;

import java.time.Instant;

@Data
public class UserDto {
    private Long id;
    private String login;
    private Role role;
    private String email;
    private Instant createdAt;
}
