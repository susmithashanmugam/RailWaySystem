
package com.railway.login.service;

import com.railway.login.dto.LoginRequest;
import com.railway.login.dto.LoginResponse;
import com.railway.login.security.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    private AuthService authService;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {

        // Manually initialize AuthService
        authService = new AuthService(
                authenticationManager,
                jwtService,
                3600000L
        );

        userDetails = User.withUsername("passenger@gmail.com")
                .password("password123")
                .authorities("ROLE_PASSENGER")
                .build();
    }

    private LoginRequest createRequest(String email, String password) {

        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);

        return request;
    }

    // Test 1: Successful passenger login
    @Test
    void loginShouldReturnTokenSuccessfully() {

        LoginRequest request =
                createRequest("passenger@gmail.com", "password123");

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(userDetails);

        doReturn(List.of(new SimpleGrantedAuthority("ROLE_PASSENGER")))
                .when(authentication).getAuthorities();

        when(jwtService.generateToken(userDetails))
                .thenReturn("test-jwt-token");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("test-jwt-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600000L, response.getExpiresIn());
        assertEquals("ROLE_PASSENGER", response.getRole());

        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtService, times(1)).generateToken(userDetails);
    }

    // Test 2: Successful admin login
    @Test
    void loginShouldReturnAdminRole() {

        LoginRequest request =
                createRequest("admin@gmail.com", "admin123");

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(userDetails);

        doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .when(authentication).getAuthorities();

        when(jwtService.generateToken(userDetails))
                .thenReturn("admin-jwt-token");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("ROLE_ADMIN", response.getRole());
        assertEquals("admin-jwt-token", response.getToken());
    }

    // Test 3: Default passenger role when authority is empty
    @Test
    void loginShouldUsePassengerRoleWhenAuthorityIsEmpty() {

        LoginRequest request =
                createRequest("passenger@gmail.com", "password123");

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(userDetails);

        doReturn(List.of())
                .when(authentication).getAuthorities();

        when(jwtService.generateToken(userDetails))
                .thenReturn("test-jwt-token");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("ROLE_PASSENGER", response.getRole());
    }

    // Test 4: Invalid credentials
    @Test
    void loginShouldThrowExceptionForInvalidCredentials() {

        LoginRequest request =
                createRequest("passenger@gmail.com", "wrongpassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(
                        new BadCredentialsException("Invalid credentials")
                );

        assertThrows(
                BadCredentialsException.class,
                () -> authService.login(request)
        );

        verify(jwtService, never()).generateToken(any());
    }
}
