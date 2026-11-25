
package com.railway.login.security;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        userDetails = User.withUsername("passenger@gmail.com")
                .password("password123")
                .authorities("ROLE_PASSENGER")
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // Test 1: Continue when Authorization header is missing

    @Test
    void doFilterShouldContinueWhenAuthorizationHeaderIsMissing()
            throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
    }

    // Test 2: Continue when Authorization header is not Bearer

    @Test
    void doFilterShouldContinueWhenTokenIsNotBearer()
            throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic some-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
    }

    // Test 3: Authenticate when JWT is valid

    @Test
    void doFilterShouldAuthenticateWhenTokenIsValid()
            throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractUsername("valid-token"))
                .thenReturn("passenger@gmail.com");

        when(userDetailsService.loadUserByUsername("passenger@gmail.com"))
                .thenReturn(userDetails);

        when(jwtService.isTokenValid("valid-token", userDetails))
                .thenReturn(true);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNotNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        assertEquals(
                "passenger@gmail.com",
                SecurityContextHolder.getContext()
                        .getAuthentication().getName()
        );

        assertTrue(
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .isAuthenticated()
        );

        verify(filterChain).doFilter(request, response);
    }

    // Test 4: Do not authenticate when JWT is invalid

    @Test
    void doFilterShouldNotAuthenticateWhenTokenIsInvalid()
            throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractUsername("invalid-token"))
                .thenReturn("passenger@gmail.com");

        when(userDetailsService.loadUserByUsername("passenger@gmail.com"))
                .thenReturn(userDetails);

        when(jwtService.isTokenValid("invalid-token", userDetails))
                .thenReturn(false);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verify(filterChain).doFilter(request, response);
    }

    // Test 5: Clear security context when JWT processing throws an exception

    @Test
    void doFilterShouldClearContextWhenTokenProcessingFails()
            throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractUsername("invalid-token"))
                .thenThrow(new RuntimeException("Invalid token"));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verify(filterChain).doFilter(request, response);
    }

    // Test 6: Do not authenticate again if authentication already exists

    @Test
    void doFilterShouldNotAuthenticateWhenAuthenticationAlreadyExists()
            throws Exception {

        UsernamePasswordAuthenticationToken existingAuthentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(existingAuthentication);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractUsername("valid-token"))
                .thenReturn("passenger@gmail.com");

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertSame(
                existingAuthentication,
                SecurityContextHolder.getContext().getAuthentication()
        );

        verifyNoInteractions(userDetailsService);
        verify(filterChain).doFilter(request, response);
    }
}
