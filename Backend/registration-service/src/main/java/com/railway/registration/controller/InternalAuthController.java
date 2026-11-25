
package com.railway.registration.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.railway.registration.dto.InternalUserAuthResponse;
import com.railway.registration.entity.User;
import com.railway.registration.repository.UserRepository;

@RestController
@RequestMapping("/internal/auth")
public class InternalAuthController {

    private final UserRepository userRepository;
    private final String internalApiKey;

    // Constructor for dependency injection
    public InternalAuthController(
            UserRepository userRepository,
            @Value("${internal.auth.api-key}") String internalApiKey) {

        this.userRepository = userRepository;
        this.internalApiKey = internalApiKey;
    }

    @GetMapping("/users/{email}")
    public ResponseEntity<InternalUserAuthResponse> getUserForAuthentication(
            @PathVariable String email,
            @RequestHeader("X-INTERNAL-API-KEY") String apiKey) {

        // Validate internal API key
        if (!internalApiKey.equals(apiKey)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Invalid internal service credentials"
            );
        }

        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        // Create authentication response
        InternalUserAuthResponse response =
                new InternalUserAuthResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getPassword(),
                        user.getRole().getName()
                );

        return ResponseEntity.ok(response);
    }
}
