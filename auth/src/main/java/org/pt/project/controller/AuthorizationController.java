package org.pt.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.pt.project.dto.LoginRequest;
import org.pt.project.dto.TokenResponse;
import org.pt.project.service.AuthorizationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthorizationController {
    private final AuthorizationService authorizationService;

    @PostMapping("/token")
    public TokenResponse getToken(@Valid @RequestBody LoginRequest request) {
        return authorizationService.getToken(request);

    }
}
