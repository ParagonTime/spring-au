package org.pt.project.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRegistrationRequest {
    @NotBlank(message = "Логин не может быть пустым")
    public String login;

    @NotBlank(message = "Почта не должна быть пустой")
    @Email
    private String email;

    @NotBlank(message = "Пароль не может быть пустым")
    public String password;
}
