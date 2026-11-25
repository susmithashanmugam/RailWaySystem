
package com.railway.registration.exception;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.railway.registration.dto.RegistrationRequest;

class GlobalExceptionHandlerValidationTest {

    @Test
    void handleValidation_ShouldReturnBadRequestWithFieldErrors() {

        RegistrationRequest request = new RegistrationRequest();

        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(request, "registrationRequest");

        bindingResult.addError(
                new FieldError(
                        "registrationRequest",
                        "email",
                        "Please provide a valid email"
                )
        );

        bindingResult.addError(
                new FieldError(
                        "registrationRequest",
                        "fullName",
                        "Name is required"
                )
        );

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(null, bindingResult);

        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<Map<String, Object>> response =
                handler.handleValidation(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<String, Object> body = response.getBody();

        assertNotNull(body);
        assertEquals(400, body.get("status"));
        assertEquals("Validation failed", body.get("message"));
        assertNotNull(body.get("timestamp"));

        @SuppressWarnings("unchecked")
        Map<String, String> errors =
                (Map<String, String>) body.get("errors");

        assertEquals(2, errors.size());
        assertEquals("Please provide a valid email", errors.get("email"));
        assertEquals("Name is required", errors.get("fullName"));
    }
}
