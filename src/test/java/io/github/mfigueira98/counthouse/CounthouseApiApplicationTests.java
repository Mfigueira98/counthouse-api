package io.github.mfigueira98.counthouse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "auth.jwt-secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
)
class CounthouseApiApplicationTests {

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();

    @LocalServerPort
    private int port;

    @Test
    void loginThenMeReturnsCurrentUserAndTenant() throws Exception {
        var login = login("demo-password");
        assertEquals(200, login.statusCode(), login.body());

        JsonNode tokens = json.readTree(login.body());
        String accessToken = tokens.path("accessToken").asText();

        assertFalse(accessToken.isBlank());
        assertFalse(tokens.path("refreshToken").asText().isBlank());
        assertEquals("Bearer", tokens.path("tokenType").asText());
        assertEquals(900, tokens.path("expiresIn").asInt());
        assertEquals(604800, tokens.path("refreshExpiresIn").asInt());

        var request = HttpRequest.newBuilder(url("/api/v1/auth/me"))
            .header("Authorization", "Bearer " + accessToken)
            .GET()
            .build();

        var response = http.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), response.body());

        JsonNode me = json.readTree(response.body());
        assertEquals("ana@counthouse.example", me.at("/user/email").asText());
        assertEquals("northwind", me.at("/tenant/slug").asText());

        String tenantId = me.at("/tenant/id").asText();
        assertFalse(tenantId.isBlank());
        assertEquals(tenantId, me.at("/user/tenantId").asText());
    }

    @Test
    void loginWithWrongPasswordReturnsUnauthorized() throws Exception {
        var response = login("wrong-password");

        assertEquals(401, response.statusCode(), response.body());
        assertEquals(
            "INVALID_CREDENTIALS",
            json.readTree(response.body()).path("code").asText()
        );
    }

    @Test
    void meWithoutTokenReturnsUnauthorized() throws Exception {
        var request = HttpRequest.newBuilder(url("/api/v1/auth/me"))
            .GET()
            .build();

        var response = http.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(401, response.statusCode(), response.body());

        JsonNode problem = json.readTree(response.body());
        assertEquals("TOKEN_INVALID", problem.path("code").asText());
        assertEquals(
            "urn:counthouse:problem:token_invalid",
            problem.path("type").asText()
        );
    }

    private HttpResponse<String> login(String password) throws Exception {
        String body = json.writeValueAsString(
            java.util.Map.of("email", "ana@counthouse.example", "password", password)
        );

        var request = HttpRequest.newBuilder(url("/api/v1/auth/login"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private URI url(String path) {
        return URI.create("http://localhost:" + port + path);
    }
}
