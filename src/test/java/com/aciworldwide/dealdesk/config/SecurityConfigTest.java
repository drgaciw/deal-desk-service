package com.aciworldwide.dealdesk.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class SecurityConfigTest {

    private final SecurityConfig securityConfig = new SecurityConfig();

    @Test
    void jwtConverterPrefixesRolesForHasRoleChecks() {
        Jwt jwt = jwtWithClaims(List.of("ADMIN", "ROLE_USER", "DEAL_CREATOR"), "deals.read");

        JwtAuthenticationToken authentication =
                (JwtAuthenticationToken) securityConfig.jwtAuthenticationConverter().convert(jwt);

        assertThat(authentication).isNotNull();
        assertThat(authentication.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_ADMIN", "ROLE_USER", "ROLE_DEAL_CREATOR", "SCOPE_deals.read")
                .doesNotContain("ROLE_ROLE_USER", "ADMIN", "DEAL_CREATOR", "deals.read");
    }

    @Test
    void jwtConverterIgnoresBlankScopes() {
        Jwt jwt = jwtWithClaims(List.of("ADMIN"), "deals.read  ");

        JwtAuthenticationToken authentication =
                (JwtAuthenticationToken) securityConfig.jwtAuthenticationConverter().convert(jwt);

        assertThat(authentication).isNotNull();
        assertThat(authentication.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_ADMIN", "SCOPE_deals.read")
                .doesNotContain("", "SCOPE_");
    }

    private Jwt jwtWithClaims(List<String> roles, String scope) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("roles", roles)
                .claim("scope", scope)
                .build();
    }
}
