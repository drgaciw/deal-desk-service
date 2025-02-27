package com.aciworldwide.dealdesk.util;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class TestJwtTokens {
    private static final KeyPair keyPair;

    static {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize test JWT key pair", e);
        }
    }

    public static String createJwtToken(String subject, List<String> roles) {
        try {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(subject)
                .issuer("http://localhost:9999/test-auth")
                .claim("scope", String.join(" ", roles))
                .claim("roles", roles)
                .claim("user_id", UUID.randomUUID().toString())
                .claim("email", subject + "@example.com")
                .claim("name", "Test User")
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plusSeconds(3600)))
                .jwtID(UUID.randomUUID().toString())
                .build();

            SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
                claims);

            RSASSASigner signer = new RSASSASigner(keyPair.getPrivate());
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test JWT token", e);
        }
    }

    public static String createDealCreatorToken() {
        return createJwtToken("deal-creator", List.of("DEAL_CREATOR"));
    }

    public static String createDealViewerToken() {
        return createJwtToken("deal-viewer", List.of("DEAL_VIEWER"));
    }

    public static String createDealApproverToken() {
        return createJwtToken("deal-approver", List.of("DEAL_APPROVER"));
    }

    public static String createDealAdminToken() {
        return createJwtToken("deal-admin", List.of("DEAL_ADMIN", "DEAL_CREATOR", "DEAL_APPROVER"));
    }

    public static String createInvalidToken() {
        return "invalid.jwt.token";
    }
}