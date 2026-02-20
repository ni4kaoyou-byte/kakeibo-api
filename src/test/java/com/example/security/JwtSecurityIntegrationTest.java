package com.example.security;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        properties = {
                "spring.flyway.enabled=false",
                "spring.autoconfigure.exclude="
                        + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
        }
)
@AutoConfigureMockMvc
class JwtSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private Jwt validJwt;

    @BeforeEach
    void setUp() {
        validJwt = Jwt.withTokenValue("valid-token")
                .header("alg", "RS256")
                .claim("sub", "dev-user")
                .claim("scope", "profile")
                .issuedAt(Instant.now().minusSeconds(60))
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
    }

    @Test
    void shouldReturn401WhenTokenIsMissing() throws Exception {
        mockMvc.perform(get("/api/secure/ping"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenSwaggerUiIsRequestedWithoutToken() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenApiDocsAreRequestedWithoutToken() throws Exception {
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowActuatorHealthWithoutToken() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn401ForCqrsQueryEndpointsWithoutToken() throws Exception {
        List<String> protectedPaths = List.of(
                "/ledger/transactions",
                "/catalog/categories",
                "/planning/budgets",
                "/analytics/reports/yearly?year=2026"
        );

        for (String path : protectedPaths) {
            mockMvc.perform(get(path))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void shouldReachEndpointHandlerForCqrsQueryPathsWhenTokenIsValid() throws Exception {
        when(jwtDecoder.decode("valid-token")).thenReturn(validJwt);

        List<String> protectedPaths = List.of(
                "/ledger/transactions",
                "/catalog/categories",
                "/planning/budgets",
                "/analytics/reports/yearly?year=2026"
        );

        for (String path : protectedPaths) {
            mockMvc.perform(get(path).header("Authorization", "Bearer valid-token"))
                    .andExpect(status().isNotFound());
        }

        verify(jwtDecoder, times(protectedPaths.size())).decode("valid-token");
    }

    @Test
    void shouldReturn200WhenTokenIsValid() throws Exception {
        when(jwtDecoder.decode("valid-token")).thenReturn(validJwt);

        mockMvc.perform(get("/api/secure/ping")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk());

        verify(jwtDecoder).decode("valid-token");
    }

    @Test
    void shouldReturn401WhenTokenIsInvalid() throws Exception {
        when(jwtDecoder.decode("invalid-token")).thenThrow(new BadJwtException("invalid token"));

        mockMvc.perform(get("/api/secure/ping")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());

        verify(jwtDecoder).decode("invalid-token");
    }
}
