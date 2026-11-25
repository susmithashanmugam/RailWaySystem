package com.railway.login.controller;

import com.railway.login.dto.LoginRequest;
import com.railway.login.dto.LoginResponse;
import com.railway.login.service.AuthService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    @Mock
    private AuthService authService;

    private LoginController loginController;

    @BeforeEach
    void setUp() {
        loginController = new LoginController(authService);
    }

    // Test 1: Successful login

    @Test
    void loginShouldReturnSuccessfulResponse() {

        LoginRequest request = new LoginRequest();
        request.setEmail("passenger@gmail.com");
        request.setPassword("password123");

        LoginResponse expectedResponse = new LoginResponse(
                "test-jwt-token",
                "Bearer",
                3600000L,
                "ROLE_PASSENGER"
        );

        when(authService.login(request))
                .thenReturn(expectedResponse);

        ResponseEntity<LoginResponse> response =
                loginController.login(request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("test-jwt-token", response.getBody().getToken());
        assertEquals("Bearer", response.getBody().getTokenType());
        assertEquals("ROLE_PASSENGER", response.getBody().getRole());

        verify(authService).login(request);
    }

    // Test 2: Invalid credentials

    @Test
    void loginShouldPropagateAuthenticationException() {

        LoginRequest request = new LoginRequest();
        request.setEmail("passenger@gmail.com");
        request.setPassword("wrongpassword");

        when(authService.login(request))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(
                BadCredentialsException.class,
                () -> loginController.login(request)
        );
    }

    // Test 3: Get current passenger details

    @Test
    void getCurrentUserShouldReturnEmailAndRole() {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "passenger@gmail.com",
                        null,
                        List.of(
                                new SimpleGrantedAuthority("ROLE_PASSENGER")
                        )
                );

        ResponseEntity<Map<String, String>> response =
                loginController.getCurrentUser(authentication);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(
                "passenger@gmail.com",
                response.getBody().get("email")
        );
        assertEquals(
                "ROLE_PASSENGER",
                response.getBody().get("role")
        );
    }

    // Test 4: Default role when no authority exists

    @Test
    void getCurrentUserShouldReturnUnknownWhenRoleIsMissing() {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        "user@gmail.com",
                        null,
                        List.of()
                );

        ResponseEntity<Map<String, String>> response =
                loginController.getCurrentUser(authentication);

        assertEquals("UNKNOWN", response.getBody().get("role"));
    }
}
