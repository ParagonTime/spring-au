package org.pt.project.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String login;
    private String email;
}
