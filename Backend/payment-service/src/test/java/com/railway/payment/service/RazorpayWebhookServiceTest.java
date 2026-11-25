
// package com.railway.payment.service;

// import java.math.BigDecimal;
// import java.nio.charset.StandardCharsets;
// import java.util.Optional;

// import javax.crypto.Mac;
// import javax.crypto.spec.SecretKeySpec;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyLong;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.ArgumentMatchers.eq;
// import org.mockito.Mock;
// import static org.mockito.Mockito.doThrow;
// import static org.mockito.Mockito.lenient;
// import static org.mockito.Mockito.mock;
// import static org.mockito.Mockito.never;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.verifyNoInteractions;
// import static org.mockito.Mockito.when;
// import org.mockito.junit.jupiter.MockitoExtension;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.railway.payment.client.ReservationServiceClient;
// import com.railway.payment.dto.PaymentResultRequest;
// import com.railway.payment.entity.Payment;
// import com.railway.payment.entity.PaymentStatus;
// import com.railway.payment.exception.PaymentException;
// import com.railway.payment.repository.PaymentRepository;

// import feign.FeignException;

// @ExtendWith(MockitoExtension.class)
// class RazorpayWebhookServiceTest {

//     @Mock
//     private PaymentRepository paymentRepository;

//     @Mock
//     private ReservationServiceClient reservationServiceClient;

//     private RazorpayWebhookService webhookService;

//     private final String secret = "test-webhook-secret";
//     private final String apiKey = "test-api-key";

//     private Payment payment;

//     @BeforeEach
//     void setUp() {
//         webhookService = new RazorpayWebhookService(
//                 paymentRepository,
//                 reservationServiceClient,
//                 new ObjectMapper(),
//                 secret,
//                 apiKey
//         );

//         payment = new Payment();
//         payment.setId(1L);
//         payment.setReservationId(101L);
//         payment.setPnr("PNR123456");
//         payment.setPassengerEmail("passenger@gmail.com");
//         payment.setAmount(new BigDecimal("1500.00"));
//         payment.setStatus(PaymentStatus.PENDING);
//         payment.setTransactionReference("TXN123456");
//         payment.setRazorpayPaymentLinkId("plink_123");
//     }

//     private String generateSignature(String body) throws Exception {
//         Mac mac = Mac.getInstance("HmacSHA256");

//         SecretKeySpec keySpec = new SecretKeySpec(
//                 secret.getBytes(StandardCharsets.UTF_8),
//                 "HmacSHA256"
//         );

//         mac.init(keySpec);

//         byte[] hash = mac.doFinal(
//                 body.getBytes(StandardCharsets.UTF_8)
//         );

//         StringBuilder hex = new StringBuilder();

//         for (byte b : hash) {
//             hex.append(String.format("%02x", b));
//         }

//         return hex.toString();
//     }

//     private String createWebhookBody(
//             String event,
//             String linkId,
//             String referenceId,
//             long amount,
//             String currency,
//             String status) {

//         return """
//                 {
//                   "event": "%s",
//                   "payload": {
//                     "payment_link": {
//                       "entity": {
//                         "id": "%s",
//                         "reference_id": "%s",
//                         "amount": %d,
//                         "currency": "%s",
//                         "status": "%s"
//                       }
//                     }
//                   }
//                 }
//                 """.formatted(
//                 event,
//                 linkId,
//                 referenceId,
//                 amount,
//                 currency,
//                 status
//         );
//     }

//    private void preparePayment() {
//     lenient().when(
//             paymentRepository.findByRazorpayPaymentLinkId("plink_123"))
//             .thenReturn(Optional.of(payment));

//     lenient().when(paymentRepository.save(any(Payment.class)))
//             .thenAnswer(invocation -> invocation.getArgument(0));
// }

//     @Test
//     void processWebhook_PaymentLinkPaidSuccessfully() throws Exception {
//         String body = createWebhookBody(
//                 "payment_link.paid",
//                 "plink_123",
//                 "TXN123456",
//                 150000,
//                 "INR",
//                 "paid"
//         );

//         preparePayment();

//         webhookService.processWebhook(
//                 body, generateSignature(body), "event_001");

//         assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
//         assertEquals("event_001", payment.getRazorpayWebhookEventId());

//         verify(reservationServiceClient).updatePaymentResult(
//                 eq(101L),
//                 eq(apiKey),
//                 any(PaymentResultRequest.class)
//         );

//         verify(paymentRepository).save(payment);
//     }

//     @Test
//     void processWebhook_PaymentLinkExpired() throws Exception {
//         String body = createWebhookBody(
//                 "payment_link.expired",
//                 "plink_123",
//                 "TXN123456",
//                 150000,
//                 "INR",
//                 "expired"
//         );

//         preparePayment();

//         webhookService.processWebhook(
//                 body, generateSignature(body), "event_002");

//         assertEquals(PaymentStatus.FAILED, payment.getStatus());

//         verify(reservationServiceClient).updatePaymentResult(
//                 eq(101L), eq(apiKey), any(PaymentResultRequest.class));

//         verify(paymentRepository).save(payment);
//     }

//     @Test
//     void processWebhook_PaymentLinkCancelled() throws Exception {
//         String body = createWebhookBody(
//                 "payment_link.cancelled",
//                 "plink_123",
//                 "TXN123456",
//                 150000,
//                 "INR",
//                 "cancelled"
//         );

//         preparePayment();

//         webhookService.processWebhook(
//                 body, generateSignature(body), "event_003");

//         assertEquals(PaymentStatus.FAILED, payment.getStatus());
//         verify(paymentRepository).save(payment);
//     }

//     @Test
//     void processWebhook_InvalidSignature() {
//         String body = """
//                 {"event":"payment_link.paid"}
//                 """;

//         assertThrows(
//                 PaymentException.class,
//                 () -> webhookService.processWebhook(
//                         body, "invalid-signature", "event_001")
//         );

//         verifyNoInteractions(paymentRepository);
//     }

//     @Test
//     void processWebhook_NullSignature() {
//         String body = """
//                 {"event":"payment_link.paid"}
//                 """;

//         assertThrows(
//                 PaymentException.class,
//                 () -> webhookService.processWebhook(
//                         body, null, "event_001")
//         );
//     }

//     @Test
//     void processWebhook_UnsupportedEvent() throws Exception {
//         String body = """
//                 {"event":"payment.authorized"}
//                 """;

//         webhookService.processWebhook(
//                 body, generateSignature(body), "event_004");

//         verifyNoInteractions(paymentRepository);
//         verifyNoInteractions(reservationServiceClient);
//     }

//     @Test
//     void processWebhook_MissingPaymentDetails() throws Exception {
//         String body = """
//                 {
//                   "event": "payment_link.paid",
//                   "payload": {
//                     "payment_link": {
//                       "entity": {}
//                     }
//                   }
//                 }
//                 """;

//         assertThrows(
//                 PaymentException.class,
//                 () -> webhookService.processWebhook(
//                         body, generateSignature(body), "event_005")
//         );
//     }

//     @Test
//     void processWebhook_PaymentRecordNotFound() throws Exception {
//         String body = createWebhookBody(
//                 "payment_link.paid",
//                 "plink_123",
//                 "TXN123456",
//                 150000,
//                 "INR",
//                 "paid"
//         );

//         when(paymentRepository.findByRazorpayPaymentLinkId("plink_123"))
//                 .thenReturn(Optional.empty());

//         assertThrows(
//                 PaymentException.class,
//                 () -> webhookService.processWebhook(
//                         body, generateSignature(body), "event_006")
//         );
//     }

//     @Test
//     void processWebhook_TransactionReferenceMismatch() throws Exception {
//         String body = createWebhookBody(
//                 "payment_link.paid",
//                 "plink_123",
//                 "WRONG_REFERENCE",
//                 150000,
//                 "INR",
//                 "paid"
//         );

//         preparePayment();

//         assertThrows(
//                 PaymentException.class,
//                 () -> webhookService.processWebhook(
//                         body, generateSignature(body), "event_007")
//         );

//         verify(reservationServiceClient, never())
//                 .updatePaymentResult(anyLong(), anyString(), any());
//     }

//     @Test
//     void processWebhook_DuplicateEvent() throws Exception {
//         payment.setRazorpayWebhookEventId("event_008");

//         String body = createWebhookBody(
//                 "payment_link.paid",
//                 "plink_123",
//                 "TXN123456",
//                 150000,
//                 "INR",
//                 "paid"
//         );

//         preparePayment();

//         webhookService.processWebhook(
//                 body, generateSignature(body), "event_008");

//         verify(reservationServiceClient, never())
//                 .updatePaymentResult(anyLong(), anyString(), any());

//         verify(paymentRepository, never()).save(any());
//     }

//     @Test
//     void processWebhook_AlreadyProcessedPayment() throws Exception {
//         payment.setStatus(PaymentStatus.SUCCESS);

//         String body = createWebhookBody(
//                 "payment_link.paid",
//                 "plink_123",
//                 "TXN123456",
//                 150000,
//                 "INR",
//                 "paid"
//         );

//         preparePayment();

//         webhookService.processWebhook(
//                 body, generateSignature(body), "event_009");

//         verify(reservationServiceClient, never())
//                 .updatePaymentResult(anyLong(), anyString(), any());

//         verify(paymentRepository, never()).save(any());
//     }

//     @Test
//     void processWebhook_AmountMismatch() throws Exception {
//         String body = createWebhookBody(
//                 "payment_link.paid",
//                 "plink_123",
//                 "TXN123456",
//                 100000,
//                 "INR",
//                 "paid"
//         );

//         preparePayment();

//         assertThrows(
//                 PaymentException.class,
//                 () -> webhookService.processWebhook(
//                         body, generateSignature(body), "event_010")
//         );

//         verify(reservationServiceClient, never())
//                 .updatePaymentResult(anyLong(), anyString(), any());
//     }

//     @Test
//     void processWebhook_CurrencyMismatch() throws Exception {
//         String body = createWebhookBody(
//                 "payment_link.paid",
//                 "plink_123",
//                 "TXN123456",
//                 150000,
//                 "USD",
//                 "paid"
//         );

//         preparePayment();

//         assertThrows(
//                 PaymentException.class,
//                 () -> webhookService.processWebhook(
//                         body, generateSignature(body), "event_011")
//         );
//     }

//     @Test
//     void processWebhook_InvalidLinkStatus() throws Exception {
//         String body = createWebhookBody(
//                 "payment_link.paid",
//                 "plink_123",
//                 "TXN123456",
//                 150000,
//                 "INR",
//                 "created"
//         );

//         preparePayment();

//         assertThrows(
//                 PaymentException.class,
//                 () -> webhookService.processWebhook(
//                         body, generateSignature(body), "event_012")
//         );
//     }

//     @Test
//     void processWebhook_ReservationServiceFailure() throws Exception {
//         String body = createWebhookBody(
//                 "payment_link.paid",
//                 "plink_123",
//                 "TXN123456",
//                 150000,
//                 "INR",
//                 "paid"
//         );

//         preparePayment();

//         doThrow(mock(FeignException.class))
//                 .when(reservationServiceClient)
//                 .updatePaymentResult(
//                         eq(101L),
//                         eq(apiKey),
//                         any(PaymentResultRequest.class)
//                 );

//         assertThrows(
//                 PaymentException.class,
//                 () -> webhookService.processWebhook(
//                         body, generateSignature(body), "event_013")
//         );

//         verify(paymentRepository, never()).save(any());
//     }

//     @Test
//     void processWebhook_InvalidJson() throws Exception {
//         String body = "{invalid-json";

//         assertThrows(
//                 PaymentException.class,
//                 () -> webhookService.processWebhook(
//                         body, generateSignature(body), "event_014")
//         );
//     }
// }
