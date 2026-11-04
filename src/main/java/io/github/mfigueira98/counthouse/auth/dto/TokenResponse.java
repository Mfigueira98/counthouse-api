package io.github.mfigueira98.counthouse.auth.dto;

public record TokenResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    int expiresIn,
    int refreshExpiresIn
) {}