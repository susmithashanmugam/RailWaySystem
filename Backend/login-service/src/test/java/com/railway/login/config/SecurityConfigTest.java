
package com.railway.login.config;

import com.railway.login.security.JwtAuthenticationFilter;
import com.railway.login.service.CustomUserDetailsService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(
                userDetailsService,
                jwtAuthenticationFilter
        );
    }

    // Test 1: Password encoder should be created

    @Test
    void passwordEncoderShouldReturnBCryptEncoder() {

        PasswordEncoder passwordEncoder =
                securityConfig.passwordEncoder();

        assertNotNull(passwordEncoder);

        String encodedPassword =
                passwordEncoder.encode("password123");

        assertTrue(
                passwordEncoder.matches(
                        "password123",
                        encodedPassword
                )
        );
    }

    // Test 2: Different passwords should not match

    @Test
    void passwordEncoderShouldRejectIncorrectPassword() {

        PasswordEncoder passwordEncoder =
                securityConfig.passwordEncoder();

        String encodedPassword =
                passwordEncoder.encode("password123");

        assertFalse(
                passwordEncoder.matches(
                        "wrongpassword",
                        encodedPassword
                )
        );
    }

    // Test 3: Authentication provider should be created

    @Test
    void authenticationProviderShouldBeCreated() {

        AuthenticationProvider provider =
                securityConfig.authenticationProvider();

        assertNotNull(provider);

        assertInstanceOf(DaoAuthenticationProvider.class, provider);
    }

    // Test 4: Authentication manager should be returned

    @Test
    void authenticationManagerShouldBeReturned() throws Exception {

        AuthenticationManager expectedManager =
                mock(AuthenticationManager.class);

        when(authenticationConfiguration.getAuthenticationManager())
                .thenReturn(expectedManager);

        AuthenticationManager actualManager =
                securityConfig.authenticationManager(
                        authenticationConfiguration
                );

        assertSame(expectedManager, actualManager);

        verify(authenticationConfiguration)
                .getAuthenticationManager();
    }
}
