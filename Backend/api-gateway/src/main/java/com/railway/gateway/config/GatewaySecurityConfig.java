
package com.railway.gateway.config;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.issuer}")
    private String jwtIssuer;

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {

        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);

        SecretKey signingKey =
                new SecretKeySpec(keyBytes, "HmacSHA256");

        NimbusReactiveJwtDecoder decoder =
                NimbusReactiveJwtDecoder
                        .withSecretKey(signingKey)
                        .macAlgorithm(MacAlgorithm.HS256)
                        .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(jwtIssuer)
        );

        return decoder;
    }

    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>>
            jwtAuthenticationConverter() {

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            String role = jwt.getClaimAsString("role");

            if (role == null || role.isBlank()) {
                return List.<GrantedAuthority>of();
            }

            return List.<GrantedAuthority>of(
                    new SimpleGrantedAuthority(role)
            );
        });

        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .authorizeExchange(exchanges -> exchanges

                        // Swagger / OpenAPI documentation
                        .pathMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Public login
                        .pathMatchers(
                                HttpMethod.POST,
                                "/api/auth/login"
                        ).permitAll()

                        // Public passenger registration
                        .pathMatchers(
                                HttpMethod.POST,
                                "/api/registration/register"
                        ).permitAll()

                        // Public health check
                        .pathMatchers("/actuator/health").permitAll()

                        // Everyone can view and search trains
                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/trains/**"
                        ).permitAll()

                        // Admin-only train creation
                        .pathMatchers(
                                HttpMethod.POST,
                                "/api/trains/**"
                        ).hasAuthority("ROLE_ADMIN")

                        // Admin-only train update
                        .pathMatchers(
                                HttpMethod.PUT,
                                "/api/trains/**"
                        ).hasAuthority("ROLE_ADMIN")

                        // Admin-only partial train update
                        .pathMatchers(
                                HttpMethod.PATCH,
                                "/api/trains/**"
                        ).hasAuthority("ROLE_ADMIN")

                        // Admin-only train deletion
                        .pathMatchers(
                                HttpMethod.DELETE,
                                "/api/trains/**"
                        ).hasAuthority("ROLE_ADMIN")

                        // Admin-only staff creation
                        .pathMatchers(
                                HttpMethod.POST,
                                "/api/registration/admin/**"
                        ).hasAuthority("ROLE_ADMIN")

                        // Staff-only reservation operations
                        // Keep this BEFORE the passenger reservation rule
                        .pathMatchers(
                                "/api/reservations/staff/**"
                        ).hasAuthority("ROLE_STAFF")

                        // Passenger-only reservation operations
                        .pathMatchers(
                                "/api/reservations/**"
                        ).hasAuthority("ROLE_PASSENGER")

                        // Public Razorpay webhook
                        .pathMatchers(
                                HttpMethod.POST,
                                "/api/payments/razorpay/webhook"
                        ).permitAll()

                        // Passenger-only payment operations
                        .pathMatchers(
                                "/api/payments/**"
                        ).hasAuthority("ROLE_PASSENGER")

                        // All remaining endpoints require authentication
                        .anyExchange().authenticated()
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(
                                        jwtAuthenticationConverter()
                                )
                        )
                )

                .build();
    }
}
