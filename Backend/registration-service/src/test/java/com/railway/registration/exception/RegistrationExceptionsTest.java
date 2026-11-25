
package com.railway.registration.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import org.junit.jupiter.api.Test;

class RegistrationExceptionsTest {

    @Test
    void duplicateResourceException_ShouldStoreMessage() {

        String message = "Email already exists";

        DuplicateResourceException exception =
                new DuplicateResourceException(message);

        assertEquals(message, exception.getMessage());
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void resourceNotFoundException_ShouldStoreMessage() {

        String message = "Passenger not found";

        ResourceNotFoundException exception =
                new ResourceNotFoundException(message);

        assertEquals(message, exception.getMessage());
        assertInstanceOf(RuntimeException.class, exception);
    }
}
