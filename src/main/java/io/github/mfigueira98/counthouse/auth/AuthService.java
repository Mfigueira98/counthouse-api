package io.github.mfigueira98.counthouse.auth;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;


import io.github.mfigueira98.counthouse.auth.dto.TokenResponse;
import io.github.mfigueira98.counthouse.user.User;
import io.github.mfigueira98.counthouse.user.UserRepository;
import io.github.mfigueira98.counthouse.auth.dto.MeResponse;
import io.github.mfigueira98.counthouse.tenant.TenantRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class AuthService {

    private final UserRepository users;
    private final AccessTokenService accessTokens;
    private final RefreshTokenService refreshTokens;
    private final BCryptPasswordEncoder passwords = new BCryptPasswordEncoder();
    private final String dummyHash = passwords.encode("dummy-login-password");
    private final TenantRepository tenants;

    public AuthService(
    UserRepository users,
    AccessTokenService accessTokens,
    RefreshTokenService refreshTokens,
    TenantRepository tenants
    ) {
    this.users = users;
    this.accessTokens = accessTokens;
    this.refreshTokens = refreshTokens;
    this.tenants = tenants;
    }

    @Transactional
    public Optional<TokenResponse> login(String email, String password) {
        return verifyCredentials(email, password).map(user -> {
            Instant issuedAt = Instant.now();

            String accessToken = accessTokens.issue(user, issuedAt);
            String refreshToken = refreshTokens.issue(user.getId(), issuedAt);

            return new TokenResponse(
                accessToken,
                refreshToken,
                "Bearer",
                AccessTokenService.EXPIRES_IN_SECONDS,
                RefreshTokenService.EXPIRES_IN_SECONDS
            );
        });
    }

    @Transactional(readOnly = true)
public Optional<MeResponse> me(UUID userId, UUID tenantId) {
    return users.findByIdAndTenantId(userId, tenantId)
        .flatMap(user -> tenants.findById(tenantId).map(tenant -> {
            String primary = tenant.getThemePrimary();
            String logoUrl = tenant.getThemeLogoUrl();

            MeResponse.ThemeData theme = primary == null && logoUrl == null
                ? null
                : new MeResponse.ThemeData(primary, logoUrl);

            return new MeResponse(
                new MeResponse.UserData(
                    user.getId(),
                    user.getTenantId(),
                    user.getEmail(),
                    user.getName(),
                    user.getRole()
                ),
                new MeResponse.TenantData(
                    tenant.getId(),
                    tenant.getSlug(),
                    tenant.getName(),
                    tenant.getDefaultCurrency(),
                    theme
                )
            );
        }));
}

    private Optional<User> verifyCredentials(String email, String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            return Optional.empty();
        }

        Optional<User> user = users.findForLoginByEmail(email);
        String hash = user.map(User::getPasswordHash).orElse(dummyHash);

        if (!passwords.matches(password, hash)) {
            return Optional.empty();
        }

        return user;
    }
}
