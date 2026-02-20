package com.example.security;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.assertj.core.api.Assertions.assertThat;

class ClientIdJwtValidatorTest {

    private static final String EXPECTED_CLIENT_ID = "kakeibo-api-dev-client";

    @Test
    void shouldSucceedWhenAudienceContainsExpectedClientId() {
        ClientIdJwtValidator validator = new ClientIdJwtValidator(EXPECTED_CLIENT_ID);
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .audience(List.of("another-client", EXPECTED_CLIENT_ID))
                .issuedAt(Instant.now().minusSeconds(60))
                .expiresAt(Instant.now().plusSeconds(300))
                .build();

        assertThat(validator.validate(jwt).hasErrors()).isFalse();
    }

    @Test
    void shouldSucceedWhenAzpMatchesExpectedClientId() {
        ClientIdJwtValidator validator = new ClientIdJwtValidator(EXPECTED_CLIENT_ID);
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("azp", EXPECTED_CLIENT_ID)
                .issuedAt(Instant.now().minusSeconds(60))
                .expiresAt(Instant.now().plusSeconds(300))
                .build();

        assertThat(validator.validate(jwt).hasErrors()).isFalse();
    }

    @Test
    void shouldFailWhenNeitherAudienceNorAzpMatches() {
        ClientIdJwtValidator validator = new ClientIdJwtValidator(EXPECTED_CLIENT_ID);
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .audience(List.of("another-client"))
                .claim("azp", "another-client")
                .issuedAt(Instant.now().minusSeconds(60))
                .expiresAt(Instant.now().plusSeconds(300))
                .build();

        assertThat(validator.validate(jwt).hasErrors()).isTrue();
    }
}
