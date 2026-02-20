package com.example.security;

import java.util.List;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public class ClientIdJwtValidator implements OAuth2TokenValidator<Jwt> {

    private final String expectedClientId;

    public ClientIdJwtValidator(String expectedClientId) {
        this.expectedClientId = expectedClientId;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        List<String> audience = token.getAudience();
        String authorizedParty = token.getClaimAsString("azp");

        boolean audienceMatches = audience != null && audience.contains(expectedClientId);
        boolean azpMatches = expectedClientId.equals(authorizedParty);

        if (audienceMatches || azpMatches) {
            return OAuth2TokenValidatorResult.success();
        }

        OAuth2Error error = new OAuth2Error(
                "invalid_token",
                "The required client identifier is missing in aud or azp",
                null
        );
        return OAuth2TokenValidatorResult.failure(error);
    }
}
