package com.example.security;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

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
class OidcResourceServerIntegrationTest {

    private static final String REALM = "kakeibo-dev";
    private static final String EXPECTED_CLIENT_ID = "kakeibo-api-dev-client";
    private static final String KEY_ID = "test-key-id";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static MockWebServer mockWebServer;
    private static RSAKey rsaJwk;
    private static String issuerUri;

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        startMockOidcServerIfNeeded();
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> issuerUri);
        registry.add("app.security.jwt.expected-client-id", () -> EXPECTED_CLIENT_ID);
    }

    @AfterAll
    static void tearDown() throws IOException {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    @Test
    void shouldReturn401WhenTokenIsMissing() throws Exception {
        mockMvc.perform(get("/api/secure/ping"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn200WhenTokenIsValidAndProperlySigned() throws Exception {
        String token = signedToken(EXPECTED_CLIENT_ID, EXPECTED_CLIENT_ID);

        mockMvc.perform(get("/api/secure/ping")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn401WhenAudienceAndAzpDoNotMatchExpectedClient() throws Exception {
        String token = signedToken("different-client", "different-client");

        mockMvc.perform(get("/api/secure/ping")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    private static synchronized void startMockOidcServerIfNeeded() {
        if (mockWebServer != null) {
            return;
        }

        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();
            rsaJwk = new RSAKey.Builder((java.security.interfaces.RSAPublicKey) keyPair.getPublic())
                    .privateKey((RSAPrivateKey) keyPair.getPrivate())
                    .keyID(KEY_ID)
                    .build();

            mockWebServer = new MockWebServer();
            mockWebServer.start();
            issuerUri = mockWebServer.url("/realms/" + REALM).toString();
            issuerUri = issuerUri.endsWith("/") ? issuerUri.substring(0, issuerUri.length() - 1) : issuerUri;

            String openIdConfiguration = "{"
                    + "\"issuer\":\"" + issuerUri + "\","
                    + "\"jwks_uri\":\"" + issuerUri + "/protocol/openid-connect/certs\","
                    + "\"token_endpoint\":\"" + issuerUri + "/protocol/openid-connect/token\""
                    + "}";
            String jwksBody = OBJECT_MAPPER.writeValueAsString(new JWKSet(rsaJwk.toPublicJWK()).toJSONObject());

            mockWebServer.setDispatcher(new Dispatcher() {
                @Override
                public MockResponse dispatch(RecordedRequest request) {
                    String path = request.getPath();
                    if (path == null) {
                        return new MockResponse().setResponseCode(404);
                    }
                    if (path.startsWith("/realms/" + REALM + "/.well-known/openid-configuration")) {
                        return new MockResponse()
                                .setResponseCode(200)
                                .addHeader("Content-Type", "application/json")
                                .setBody(openIdConfiguration);
                    }
                    if (path.startsWith("/realms/" + REALM + "/protocol/openid-connect/certs")) {
                        return new MockResponse()
                                .setResponseCode(200)
                                .addHeader("Content-Type", "application/json")
                                .setBody(jwksBody);
                    }
                    return new MockResponse().setResponseCode(404);
                }
            });
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start mock OIDC server", e);
        }
    }

    private static String signedToken(String audience, String azp) {
        try {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .jwtID(UUID.randomUUID().toString())
                    .issuer(issuerUri)
                    .subject("dev-user")
                    .audience(audience)
                    .claim("azp", azp)
                    .issueTime(Date.from(Instant.now().minusSeconds(30)))
                    .expirationTime(Date.from(Instant.now().plusSeconds(300)))
                    .build();

            SignedJWT jwt = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256)
                            .type(JOSEObjectType.JWT)
                            .keyID(KEY_ID)
                            .build(),
                    claims
            );

            JWSSigner signer = new RSASSASigner(rsaJwk.toPrivateKey());
            jwt.sign(signer);
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Failed to sign JWT", e);
        }
    }
}
