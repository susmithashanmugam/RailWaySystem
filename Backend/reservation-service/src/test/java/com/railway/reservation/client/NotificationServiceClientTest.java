
package com.railway.reservation.client;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceClientTest {

    @Test
    void notificationClient_ShouldHaveCorrectFeignConfiguration() {

        FeignClient annotation =
                NotificationServiceClient.class.getAnnotation(FeignClient.class);

        assertNotNull(annotation);
        assertEquals("messaging-service", annotation.name());
    }

    @Test
    void sendBookingNotification_ShouldHaveCorrectEndpoint() throws Exception {

        Method method = NotificationServiceClient.class.getMethod(
                "sendBookingNotification",
                String.class,
                NotificationServiceClient.NotificationRequest.class
        );

        PostMapping mapping = method.getAnnotation(PostMapping.class);

        assertNotNull(mapping);
        assertEquals(
                "/api/notifications/booking",
                mapping.value()[0]
        );
    }

    @Test
    void sendBookingNotification_ShouldHaveCorrectParameters() throws Exception {

        Method method = NotificationServiceClient.class.getMethod(
                "sendBookingNotification",
                String.class,
                NotificationServiceClient.NotificationRequest.class
        );

        Parameter[] parameters = method.getParameters();

        assertEquals(2, parameters.length);

        RequestHeader header =
                parameters[0].getAnnotation(RequestHeader.class);

        assertNotNull(header);
        assertEquals("X-INTERNAL-API-KEY", header.value());

        RequestBody body =
                parameters[1].getAnnotation(RequestBody.class);

        assertNotNull(body);
    }

    @Test
    void notificationRequest_ShouldStoreCorrectValues() {

        NotificationServiceClient.NotificationRequest request =
                new NotificationServiceClient.NotificationRequest(
                        "passenger@gmail.com",
                        "Your booking is confirmed"
                );

        assertEquals("passenger@gmail.com", request.recipientEmail());

        assertEquals(
                "Your booking is confirmed",
                request.message()
        );
    }

    @Test
    void notificationRequest_ShouldSupportEquality() {

        NotificationServiceClient.NotificationRequest request1 =
                new NotificationServiceClient.NotificationRequest(
                        "test@gmail.com",
                        "Booking confirmed"
                );

        NotificationServiceClient.NotificationRequest request2 =
                new NotificationServiceClient.NotificationRequest(
                        "test@gmail.com",
                        "Booking confirmed"
                );

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }
}
