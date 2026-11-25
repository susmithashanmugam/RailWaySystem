
package com.railway.login.service;

import com.railway.login.dto.LoginRequest;
import com.railway.login.dto.LoginResponse;
import com.railway.login.security.JwtService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final long expiration;

    // Constructor injection
    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            @Value("${jwt.expiration}") long expiration) {

        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.expiration = expiration;
    }

    // Authenticate user and generate JWT
    public LoginResponse login(LoginRequest request) {

        // Step 1: Validate email and password
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                );

        // Step 2: Get authenticated user details
        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();

        // Step 3: Generate JWT token
        String token = jwtService.generateToken(userDetails);

        // Step 4: Get authenticated user's role
        String role = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_PASSENGER");

        // Step 5: Return login response
        return new LoginResponse(
                token,
                "Bearer",
                expiration,
                role
        );
    }
}