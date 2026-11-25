
package com.railway.login.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.issuer}")
    private String issuer;

    // Create signing key from plain-text secret
    private SecretKey getSigningKey() {

        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Generate JWT token with user role
    public String generateToken(UserDetails userDetails) {

        Date currentDate = new Date();

        Date expirationDate =
                new Date(currentDate.getTime() + expiration);

        String role = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_PASSENGER");

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuer(issuer)
                .claim("role", role)
                .issuedAt(currentDate)
                .expiration(expirationDate)
                .signWith(getSigningKey())
                .compact();
    }

    // Extract all claims from token
    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Extract email from token
    public String extractUsername(String token) {

        return extractAllClaims(token).getSubject();
    }

    // Extract role from token
    public String extractRole(String token) {

        return extractAllClaims(token).get("role", String.class);
    }

    // Validate token
    public boolean isTokenValid(
            String token,
            UserDetails userDetails) {

        Claims claims = extractAllClaims(token);

        String username = claims.getSubject();

        Date expirationDate = claims.getExpiration();

        String tokenIssuer = claims.getIssuer();

        return username.equals(userDetails.getUsername())
                && expirationDate.after(new Date())
                && issuer.equals(tokenIssuer);
    }
}
