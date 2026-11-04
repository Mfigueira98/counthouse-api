package io.github.mfigueira98.counthouse.auth;

import java.time.Instant;
import java.util.Base64;

import javax.crypto.spec.SecretKeySpec;

import io.github.mfigueira98.counthouse.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

@Service
public class AccessTokenService {

    static final int EXPIRES_IN_SECONDS = 900;

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;

    public AccessTokenService(@Value("${auth.jwt-secret}") String secret) {
        byte[] key = Base64.getDecoder().decode(secret);

        if (key.length < 32) {
            throw new IllegalArgumentException("JWT secret must contain at least 32 bytes");
        }

        SecretKeySpec signingKey = new SecretKeySpec(key, "HmacSHA256");

        this.encoder = NimbusJwtEncoder.withSecretKey(signingKey).build();

        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(signingKey)
            .macAlgorithm(MacAlgorithm.HS256)
            .build();

        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
            JwtValidators.createDefault(),
            jwt -> {
                Instant expiresAt = jwt.getExpiresAt();

                if (expiresAt == null) {
                    return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("TOKEN_INVALID", "Missing expiration", null)
                    );
                }

                if (!expiresAt.isAfter(Instant.now())) {
                    return OAuth2TokenValidatorResult.failure(
                        new OAuth2Error("TOKEN_EXPIRED", "Token expired", null)
                    );
                }

                return OAuth2TokenValidatorResult.success();
            }
        ));

        this.decoder = jwtDecoder;
    }

    public String issue(User user, Instant issuedAt) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .subject(user.getId().toString())
            .claim("tenantId", user.getTenantId().toString())
            .claim("role", user.getRole().name())
            .issuedAt(issuedAt)
            .expiresAt(issuedAt.plusSeconds(EXPIRES_IN_SECONDS))
            .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return encoder.encode(JwtEncoderParameters.from(header, claims))
            .getTokenValue();
    }

    public Jwt validate(String token) {
        return decoder.decode(token);
    }
}
