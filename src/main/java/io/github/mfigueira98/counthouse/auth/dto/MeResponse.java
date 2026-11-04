package io.github.mfigueira98.counthouse.auth.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.mfigueira98.counthouse.user.UserRole;

public record MeResponse(UserData user, TenantData tenant) {

    public record UserData(
        UUID id,
        UUID tenantId,
        String email,
        String name,
        UserRole role
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record TenantData(
        UUID id,
        String slug,
        String name,
        String defaultCurrency,
        ThemeData theme
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ThemeData(String primary, String logoUrl) {}
}
