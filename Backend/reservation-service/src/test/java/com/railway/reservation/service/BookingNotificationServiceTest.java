
// package com.railway.reservation.service;

// import org.junit.jupiter.api.Test;

// import java.util.concurrent.CompletableFuture;

// import static org.junit.jupiter.api.Assertions.*;

// class BookingNotificationServiceTest {

//     private final BookingNotificationService notificationService =
//             new BookingNotificationService();

//     @Test
//     void sendBookingConfirmation_Success() {

//         CompletableFuture<Void> result =
//                 notificationService.sendBookingConfirmation(
//                         "PNR123456",
//                         "passenger@gmail.com"
//                 );

//         assertNotNull(result);
//         assertTrue(result.isDone());
//         assertNull(result.join());
//     }

//     @Test
//     void sendBookingConfirmation_NullValues() {

//         CompletableFuture<Void> result =
//                 notificationService.sendBookingConfirmation(null, null);

//         assertNotNull(result);
//         assertTrue(result.isDone());
//         assertNull(result.join());
//     }
// }
