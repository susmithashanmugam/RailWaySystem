
package com.railway.registration.entity;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserTest {

    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        user = new User();
        role = new Role();

        role.setId(2L);
        role.setName("ROLE_PASSENGER");
    }

    @Test
    void testUserGettersAndSetters() {

        LocalDateTime createdAt = LocalDateTime.now();

        user.setId(1L);
        user.setFullName("Test Passenger");
        user.setEmail("test@gmail.com");
        user.setPhoneNumber("9876543210");
        user.setPassword("hashedPassword");
        user.setRole(role);
        user.setCreatedAt(createdAt);

        assertEquals(1L, user.getId());
        assertEquals("Test Passenger", user.getFullName());
        assertEquals("test@gmail.com", user.getEmail());
        assertEquals("9876543210", user.getPhoneNumber());
        assertEquals("hashedPassword", user.getPassword());
        assertSame(role, user.getRole());
        assertEquals(createdAt, user.getCreatedAt());
    }

    @Test
    void testNoArgsConstructor() {

        User newUser = new User();

        assertNotNull(newUser);
        assertNull(newUser.getId());
        assertNull(newUser.getFullName());
        assertNull(newUser.getCreatedAt());
    }

    @Test
    void testAllArgsConstructor() {

        LocalDateTime createdAt = LocalDateTime.now();

        User newUser = new User(
                1L,
                "Test Passenger",
                "test@gmail.com",
                "9876543210",
                "hashedPassword",
                role,
                createdAt
        );

        assertEquals(1L, newUser.getId());
        assertEquals("Test Passenger", newUser.getFullName());
        assertEquals("test@gmail.com", newUser.getEmail());
        assertEquals("9876543210", newUser.getPhoneNumber());
        assertEquals("hashedPassword", newUser.getPassword());
        assertSame(role, newUser.getRole());
        assertEquals(createdAt, newUser.getCreatedAt());
    }

    @Test
    void testOnCreateSetsCreatedAt() {

        assertNull(user.getCreatedAt());

        user.onCreate();

        assertNotNull(user.getCreatedAt());
        assertFalse(user.getCreatedAt().isAfter(LocalDateTime.now()));
    }
}
