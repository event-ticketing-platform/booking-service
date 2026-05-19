package ee.ut.eventticketing.booking_service.security;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/actuator/health",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/events/**", "/ticket-types/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder(JwtProperties properties) {
        byte[] secret = properties.secret().getBytes(StandardCharsets.UTF_8);
        SecretKey secretKey = new SecretKeySpec(secret, "HmacSHA256");

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(jwtValidator(properties));
        return decoder;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
        return converter;
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        String role = jwt.getClaimAsString("role");
        String scope = jwt.getClaimAsString("scope");

        Stream<String> roleAuthorities = Stream.concat(
                roles == null ? Stream.empty() : roles.stream(),
                role == null || role.isBlank() ? Stream.empty() : Stream.of(role))
                .map(this::normalizeRole);
        Stream<String> scopeAuthorities = scope == null || scope.isBlank()
                ? Stream.empty()
                : Stream.of(scope.split(" ")).map(value -> "SCOPE_" + value);

        return Stream.concat(roleAuthorities, scopeAuthorities)
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
    }

    private OAuth2TokenValidator<Jwt> jwtValidator(JwtProperties properties) {
        JwtTimestampValidator timestampValidator = new JwtTimestampValidator();
        return jwt -> {
            OAuth2TokenValidatorResult timestampResult = timestampValidator.validate(jwt);
            if (timestampResult.hasErrors()) {
                return timestampResult;
            }

            String issuer = jwt.getClaimAsString("iss");
            if (issuer == null || properties.issuer().equals(issuer)) {
                return OAuth2TokenValidatorResult.success();
            }

            return OAuth2TokenValidatorResult.failure(new OAuth2Error(
                    "invalid_token",
                    "The token issuer is not trusted",
                    null));
        };
    }

    private String normalizeRole(String role) {
        String normalized = role.startsWith("ROLE_") ? role.substring("ROLE_".length()) : role;
        if ("USER".equals(normalized)) {
            normalized = "CUSTOMER";
        }
        return "ROLE_" + normalized;
    }
}
