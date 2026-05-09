package com.aciworldwide.dealdesk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private static final String ROLE_PREFIX = "ROLE_";
    private static final String SCOPE_PREFIX = "SCOPE_";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new JwtGrantedAuthoritiesConverter());
        return converter;
    }

    /**
     * Custom converter to extract authorities from JWT claims.
     * Supports both 'roles' claim (as a list) and 'scope' claim (as space-separated string).
     */
    private static class JwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            Set<GrantedAuthority> authorities = new LinkedHashSet<>();

            // Extract from 'roles' claim (list format)
            Object rolesClaim = jwt.getClaim("roles");
            if (rolesClaim instanceof List<?>) {
                List<String> roles = ((List<?>) rolesClaim).stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
                authorities.addAll(roles.stream()
                    .map(SecurityConfig::roleAuthority)
                    .toList());
            }

            // Extract from 'scope' claim (space-separated string format)
            String scopeClaim = jwt.getClaimAsString("scope");
            if (scopeClaim != null && !scopeClaim.isEmpty()) {
                String[] scopes = scopeClaim.split(" ");
                for (String scope : scopes) {
                    if (!scope.isBlank()) {
                        authorities.add(scopeAuthority(scope));
                    }
                }
            }

            return new ArrayList<>(authorities);
        }
    }

    private static SimpleGrantedAuthority roleAuthority(String role) {
        String authority = role.startsWith(ROLE_PREFIX) ? role : ROLE_PREFIX + role;
        return new SimpleGrantedAuthority(authority);
    }

    private static SimpleGrantedAuthority scopeAuthority(String scope) {
        String authority = scope.startsWith(SCOPE_PREFIX) ? scope : SCOPE_PREFIX + scope;
        return new SimpleGrantedAuthority(authority);
    }
}