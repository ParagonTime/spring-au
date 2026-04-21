package org.pt.project.dto;

import java.time.Instant;

public record TokenResponse(String accessToken, Instant expiresAt) {}
