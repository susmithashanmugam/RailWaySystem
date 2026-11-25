
package com.railway.registration.exception;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleDuplicateResource_ShouldReturnConflict() {

        DuplicateResourceException exception =
                new DuplicateResourceException("Email already exists");

        ResponseEntity<Map<String, Object>> response =
                handler.handleDuplicateResource(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Email already exists",
                response.getBody().get("message"));
        assertEquals(409, response.getBody().get("status"));
    }

    @Test
    void handleNotFound_ShouldReturnNotFound() {

        ResourceNotFoundException exception =
                new ResourceNotFoundException("Passenger not found");

        ResponseEntity<Map<String, Object>> response =
                handler.handleNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Passenger not found",
                response.getBody().get("message"));
        assertEquals(404, response.getBody().get("status"));
    }

    @Test
    void handleDatabaseConflict_ShouldReturnConflict() {

        DataIntegrityViolationException exception =
                new DataIntegrityViolationException("Database constraint");

        ResponseEntity<Map<String, Object>> response =
                handler.handleDatabaseConflict(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(
                "Email or phone number is already registered",
                response.getBody().get("message")
        );
    }
}
