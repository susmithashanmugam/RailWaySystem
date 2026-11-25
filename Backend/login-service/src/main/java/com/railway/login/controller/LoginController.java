
package com.railway.login.controller;

import com.railway.login.dto.LoginRequest;
import com.railway.login.dto.LoginResponse;
import com.railway.login.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private final AuthService authService;

    // Constructor injection
    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    // Login endpoint
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    // Get currently authenticated user's details
    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getCurrentUser(
            Authentication authentication) {

        String role = authentication.getAuthorities()
                .stream()
                .map(authority -> authority.getAuthority())
                .findFirst()
                .orElse("UNKNOWN");

        return ResponseEntity.ok(
                Map.of(
                        "email", authentication.getName(),
                        "role", role
                )
        );
    }
}
