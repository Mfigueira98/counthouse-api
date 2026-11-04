package io.github.mfigueira98.counthouse.auth;

import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.Locale;


import io.github.mfigueira98.counthouse.auth.dto.LoginRequest;
import jakarta.validation.Valid;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
public class AuthController {

    private final AuthService authService;
    private final AccessTokenService accessTokens;

    public AuthController(AuthService authService, AccessTokenService accessTokens) {
    this.authService = authService;
    this.accessTokens = accessTokens;
    }

    @PostMapping("/api/v1/auth/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        var tokens = authService.login(request.email(), request.password());

        if (tokens.isEmpty()) {
            return problemResponse(problem(
                HttpStatus.UNAUTHORIZED, "Invalid credentials", "INVALID_CREDENTIALS"
            ));
        }

        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .body(tokens.get());
    }

    @GetMapping("/api/v1/auth/me")
    public ResponseEntity<?> me(
    @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false)
    String authorization
    ) {
        if (authorization == null
            || !authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return unauthorized("Token invalid", "TOKEN_INVALID");
        }

        String token = authorization.substring(7).strip();
        if (token.isEmpty()) {
            return unauthorized("Token invalid", "TOKEN_INVALID");
        }

        UUID userId;
        UUID tenantId;

        try {
            var jwt = accessTokens.validate(token);
            String subject = jwt.getSubject();
            String tenantClaim = jwt.getClaimAsString("tenantId");

            if (subject == null || tenantClaim == null) {
                return unauthorized("Token invalid", "TOKEN_INVALID");
            }

            userId = UUID.fromString(subject);
            tenantId = UUID.fromString(tenantClaim);
        } catch (JwtValidationException exception) {
            boolean expired = exception.getErrors().stream()
                .anyMatch(error -> "TOKEN_EXPIRED".equals(error.getErrorCode()));

            return expired
                ? unauthorized("Token expired", "TOKEN_EXPIRED")
                : unauthorized("Token invalid", "TOKEN_INVALID");
        } catch (JwtException | IllegalArgumentException exception) {
            return unauthorized("Token invalid", "TOKEN_INVALID");
        }

        var response = authService.me(userId, tenantId);
        if (response.isEmpty()) {
            return unauthorized("Token invalid", "TOKEN_INVALID");
        }

        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .body(response.get());
    }

    private ResponseEntity<ProblemDetail> unauthorized(String title, String code) {
        return problemResponse(problem(HttpStatus.UNAUTHORIZED, title, code));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> invalidFields(MethodArgumentNotValidException exception) {
        ProblemDetail body = problem(
            HttpStatus.BAD_REQUEST, "Validation failed", "VALIDATION_FAILED"
        );

        var errors = exception.getBindingResult().getFieldErrors().stream()
            .map(error -> Map.of(
                "field", error.getField(),
                "code", "NotEmpty".equals(error.getCode()) ? "REQUIRED" : "INVALID_FORMAT",
                "message", error.getDefaultMessage() == null
                    ? "Invalid value" : error.getDefaultMessage()
            ))
            .toList();

        body.setProperty("errors", errors);
        return problemResponse(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> invalidJson() {
        ProblemDetail body = problem(
            HttpStatus.BAD_REQUEST, "Validation failed", "VALIDATION_FAILED"
        );
        body.setDetail("The request body must be valid JSON.");
        return problemResponse(body);
    }

    private ProblemDetail problem(HttpStatus status, String title, String code) {
        ProblemDetail body = ProblemDetail.forStatus(status);
        body.setType(URI.create("urn:counthouse:problem:" + code.toLowerCase(Locale.ROOT)));
        body.setTitle(title);
        body.setProperty("code", code);
        body.setProperty("traceId", UUID.randomUUID().toString().replace("-", ""));
        return body;
    }  

    private ResponseEntity<ProblemDetail> problemResponse(ProblemDetail body) {
        return ResponseEntity.status(body.getStatus())
            .contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .body(body);
    }
}
