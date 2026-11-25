
package com.railway.login.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    private UserDetails userDetails;

    private final String secret =
            "RailwayReservationSystemSecretKeyForTesting123456";

    private final String issuer = "railway-reservation-system";

    private final long expiration = 3600000L;

    @BeforeEach
    void setUp() {

        jwtService = new JwtService();

        ReflectionTestUtils.setField(jwtService, "secret", secret);
        ReflectionTestUtils.setField(jwtService, "expiration", expiration);
        ReflectionTestUtils.setField(jwtService, "issuer", issuer);

        userDetails = User.withUsername("passenger@gmail.com")
                .password("password123")
                .authorities("ROLE_PASSENGER")
                .build();
    }

    // Test 1: Generate JWT token

    @Test
    void generateTokenShouldReturnValidToken() {

        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    // Test 2: Extract username from token

    @Test
    void extractUsernameShouldReturnCorrectEmail() {

        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertEquals("passenger@gmail.com", username);
    }

    // Test 3: Extract role from token

    @Test
    void extractRoleShouldReturnCorrectRole() {

        String token = jwtService.generateToken(userDetails);

        String role = jwtService.extractRole(token);

        assertEquals("ROLE_PASSENGER", role);
    }

    // Test 4: Validate a correct token

    @Test
    void isTokenValidShouldReturnTrueForValidToken() {

        String token = jwtService.generateToken(userDetails);

        boolean result = jwtService.isTokenValid(token, userDetails);

        assertTrue(result);
    }

    // Test 5: Reject token belonging to another username

    @Test
    void isTokenValidShouldReturnFalseForDifferentUsername() {

        String token = jwtService.generateToken(userDetails);

        UserDetails anotherUser = User.withUsername("admin@gmail.com")
                .password("admin123")
                .authorities("ROLE_ADMIN")
                .build();

        boolean result = jwtService.isTokenValid(token, anotherUser);

        assertFalse(result);
    }

    // Test 6: Reject token with incorrect issuer

    @Test
    void isTokenValidShouldReturnFalseForDifferentIssuer() {

        String token = createTokenWithIssuer("wrong-issuer");

        boolean result = jwtService.isTokenValid(token, userDetails);

        assertFalse(result);
    }

    // Test 7: Reject expired token

    @Test
    void expiredTokenShouldThrowException() {

        SecretKey signingKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );

        Date now = new Date();

        String expiredToken = Jwts.builder()
                .subject(userDetails.getUsername())
                .issuer(issuer)
                .issuedAt(new Date(now.getTime() - 10000))
                .expiration(new Date(now.getTime() - 5000))
                .signWith(signingKey)
                .compact();

        assertThrows(
                ExpiredJwtException.class,
                () -> jwtService.isTokenValid(expiredToken, userDetails)
        );
    }

    // Test 8: Reject malformed token

    @Test
    void invalidTokenShouldThrowException() {

        assertThrows(
                Exception.class,
                () -> jwtService.extractUsername("invalid.jwt.token")
        );
    }

    private String createTokenWithIssuer(String tokenIssuer) {

        SecretKey signingKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );

        Date now = new Date();

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuer(tokenIssuer)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(signingKey)
                .compact();
    }
}
