
package com.railway.login.service;

import com.railway.login.client.RegistrationServiceClient;
import com.railway.login.dto.InternalUserAuthResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private RegistrationServiceClient registrationServiceClient;

    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {

        customUserDetailsService =
                new CustomUserDetailsService(registrationServiceClient);

        ReflectionTestUtils.setField(
                customUserDetailsService,
                "internalApiKey",
                "test-api-key"
        );
    }

    // Test 1: Successfully load user details

    @Test
    void loadUserByUsernameShouldReturnUserDetails() {

        InternalUserAuthResponse user = new InternalUserAuthResponse();

        user.setEmail("passenger@gmail.com");
        user.setPasswordHash("encoded-password");
        user.setRole("ROLE_PASSENGER");

        when(registrationServiceClient.getUserForAuthentication(
                "passenger@gmail.com",
                "test-api-key"
        )).thenReturn(user);

        UserDetails result =
                customUserDetailsService.loadUserByUsername(
                        "passenger@gmail.com"
                );

        assertNotNull(result);
        assertEquals("passenger@gmail.com", result.getUsername());
        assertEquals("encoded-password", result.getPassword());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_PASSENGER")));

        verify(registrationServiceClient).getUserForAuthentication(
                "passenger@gmail.com",
                "test-api-key"
        );
    }

    // Test 2: Successfully load admin role

    @Test
    void loadUserByUsernameShouldReturnAdminRole() {

        InternalUserAuthResponse user = new InternalUserAuthResponse();

        user.setEmail("admin@gmail.com");
        user.setPasswordHash("encoded-password");
        user.setRole("ROLE_ADMIN");

        when(registrationServiceClient.getUserForAuthentication(
                "admin@gmail.com",
                "test-api-key"
        )).thenReturn(user);

        UserDetails result =
                customUserDetailsService.loadUserByUsername("admin@gmail.com");

        assertEquals("admin@gmail.com", result.getUsername());

        assertTrue(result.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN")));
    }

    // Test 3: Throw exception when user is not found

    @Test
    void loadUserByUsernameShouldThrowExceptionWhenUserNotFound() {

        when(registrationServiceClient.getUserForAuthentication(
                "unknown@gmail.com",
                "test-api-key"
        )).thenThrow(new RuntimeException("User not found"));

        assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(
                        "unknown@gmail.com"
                )
        );
    }

    // Test 4: Throw exception when registration service fails

    @Test
    void loadUserByUsernameShouldThrowExceptionWhenServiceFails() {

        when(registrationServiceClient.getUserForAuthentication(
                "passenger@gmail.com",
                "test-api-key"
        )).thenThrow(new RuntimeException("Service unavailable"));

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(
                        "passenger@gmail.com"
                )
        );

        assertEquals("Invalid email or password", exception.getMessage());
    }
}
