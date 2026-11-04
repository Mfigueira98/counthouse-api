package io.github.mfigueira98.counthouse.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {

    static final int EXPIRES_IN_SECONDS = 604_800;

    private final RefreshTokenRepository repository;
    private final SecureRandom random = new SecureRandom();

    public RefreshTokenService(RefreshTokenRepository repository) {
        this.repository = repository;
    }

    public String issue(UUID userId, Instant issuedAt) {
        byte[] randomBytes = new byte[32];
        random.nextBytes(randomBytes);

        String token = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(randomBytes);

        Instant expiresAt = issuedAt.plusSeconds(EXPIRES_IN_SECONDS);
        repository.save(new RefreshToken(userId, hashToken(token), expiresAt));

        return token;
    }

    private String hashToken(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }
}
