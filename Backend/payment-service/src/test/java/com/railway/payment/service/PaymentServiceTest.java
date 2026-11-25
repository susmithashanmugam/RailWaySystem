
package com.railway.payment.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.railway.payment.client.RazorpayClient;
import com.railway.payment.client.ReservationServiceClient;
import com.railway.payment.dto.CreatePaymentRequest;
import com.railway.payment.dto.PaymentResponse;
import com.railway.payment.dto.ReservationPaymentDetails;
import com.railway.payment.entity.Payment;
import com.railway.payment.entity.PaymentMethod;
import com.railway.payment.entity.PaymentStatus;
import com.railway.payment.exception.PaymentException;
import com.railway.payment.repository.PaymentRepository;

import feign.FeignException;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ReservationServiceClient reservationServiceClient;

    @Mock
    private RazorpayClient razorpayClient;

    @InjectMocks
    private PaymentService paymentService;

    private CreatePaymentRequest request;
    private ReservationPaymentDetails reservation;

    private final String email = "passenger@gmail.com";
    private final String apiKey = "test-api-key";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                paymentService, "internalApiKey", apiKey);

        request = new CreatePaymentRequest();
        request.setReservationId(101L);
        request.setPaymentMethod(PaymentMethod.UPI);

        reservation = new ReservationPaymentDetails(
                101L,
                "PNR123456",
                email,
                new BigDecimal("1500.00"),
                "PENDING_PAYMENT"
        );
    }

    private void prepareSuccessfulReservation() {

        lenient().when(
                reservationServiceClient.getReservationPaymentDetails(101L))
                .thenReturn(reservation);

        lenient().when(paymentRepository.existsByReservationIdAndStatus(
                101L, PaymentStatus.SUCCESS))
                .thenReturn(false);

        lenient().when(paymentRepository.existsByReservationIdAndStatus(
                101L, PaymentStatus.PENDING))
                .thenReturn(false);

        lenient().when(paymentRepository.saveAndFlush(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        lenient().when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void processPayment_Success() {

        prepareSuccessfulReservation();

        PaymentResponse response =
                paymentService.processPayment(request, email);

        assertNotNull(response);
        assertEquals(101L, response.getReservationId());
        assertEquals("PNR123456", response.getPnr());
        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
        assertEquals(new BigDecimal("1500.00"), response.getAmount());

        verify(reservationServiceClient)
                .updatePaymentResult(eq(101L), eq(apiKey), any());
    }

    @Test
    void processPayment_SimulatedFailure() {

        prepareSuccessfulReservation();
        request.setSimulateFailure(true);

        PaymentResponse response =
                paymentService.processPayment(request, email);

        assertNotNull(response);
        assertEquals(PaymentStatus.FAILED, response.getStatus());

        verify(reservationServiceClient)
                .updatePaymentResult(eq(101L), eq(apiKey), any());
    }

    @Test
    void processPayment_ReservationNotFound() {

        when(reservationServiceClient.getReservationPaymentDetails(101L))
                .thenThrow(mock(FeignException.NotFound.class));

        assertThrows(
                PaymentException.class,
                () -> paymentService.processPayment(request, email)
        );

        verifyNoInteractions(paymentRepository);
    }

    @Test
    void processPayment_WrongPassenger() {

        prepareSuccessfulReservation();

        assertThrows(
                PaymentException.class,
                () -> paymentService.processPayment(
                        request, "another@gmail.com")
        );

        verify(paymentRepository, never()).saveAndFlush(any());
    }

    @Test
    void processPayment_ReservationNotPending() {

        reservation = new ReservationPaymentDetails(
                101L,
                "PNR123456",
                email,
                new BigDecimal("1500.00"),
                "CONFIRMED"
        );

        when(reservationServiceClient.getReservationPaymentDetails(101L))
                .thenReturn(reservation);

        assertThrows(
                PaymentException.class,
                () -> paymentService.processPayment(request, email)
        );

        verify(paymentRepository, never()).saveAndFlush(any());
    }

    @Test
    void processPayment_AlreadySuccessful() {

        when(reservationServiceClient.getReservationPaymentDetails(101L))
                .thenReturn(reservation);

        when(paymentRepository.existsByReservationIdAndStatus(
                101L, PaymentStatus.SUCCESS))
                .thenReturn(true);

        assertThrows(
                PaymentException.class,
                () -> paymentService.processPayment(request, email)
        );

        verify(paymentRepository, never()).saveAndFlush(any());
    }

    @Test
    void processPayment_AlreadyPending() {

        when(reservationServiceClient.getReservationPaymentDetails(101L))
                .thenReturn(reservation);

        when(paymentRepository.existsByReservationIdAndStatus(
                101L, PaymentStatus.SUCCESS))
                .thenReturn(false);

        when(paymentRepository.existsByReservationIdAndStatus(
                101L, PaymentStatus.PENDING))
                .thenReturn(true);

        assertThrows(
                PaymentException.class,
                () -> paymentService.processPayment(request, email)
        );

        verify(paymentRepository, never()).saveAndFlush(any());
    }

    @Test
    void processPayment_ReservationServiceFailure() {

        prepareSuccessfulReservation();

        doThrow(mock(FeignException.class))
                .when(reservationServiceClient)
                .updatePaymentResult(eq(101L), eq(apiKey), any());

        assertThrows(
                PaymentException.class,
                () -> paymentService.processPayment(request, email)
        );
    }

    @Test
    void processPayment_RazorpaySuccess() {

        prepareSuccessfulReservation();
        request.setPaymentMethod(PaymentMethod.RAZORPAY);

        when(razorpayClient.createPaymentLink(
                eq(150000L), anyString(), anyString()))
                .thenReturn(Map.of(
                        "id", "plink_123",
                        "short_url", "https://rzp.io/test"
                ));

        PaymentResponse response =
                paymentService.processPayment(request, email);

        assertNotNull(response);
        assertEquals(PaymentStatus.PENDING, response.getStatus());
        assertEquals("https://rzp.io/test", response.getPaymentLinkUrl());

        verify(razorpayClient).createPaymentLink(
                eq(150000L), anyString(), anyString());
    }

    @Test
    void processPayment_RazorpayInvalidLinkResponse() {

        prepareSuccessfulReservation();
        request.setPaymentMethod(PaymentMethod.RAZORPAY);

        when(razorpayClient.createPaymentLink(
                anyLong(), anyString(), anyString()))
                .thenReturn(Map.of("id", "plink_123"));

        assertThrows(
                PaymentException.class,
                () -> paymentService.processPayment(request, email)
        );

        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void processPayment_RazorpayClientFailure() {

        prepareSuccessfulReservation();
        request.setPaymentMethod(PaymentMethod.RAZORPAY);

        when(razorpayClient.createPaymentLink(
                anyLong(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Razorpay unavailable"));

        assertThrows(
                PaymentException.class,
                () -> paymentService.processPayment(request, email)
        );

        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void getMyPayments_Success() {

        Payment payment = new Payment();
        payment.setId(1L);
        payment.setReservationId(101L);
        payment.setPnr("PNR123456");
        payment.setPassengerEmail(email);
        payment.setAmount(new BigDecimal("1500.00"));
        payment.setPaymentMethod(PaymentMethod.UPI);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionReference("TXN123");

        when(paymentRepository.findByPassengerEmailOrderByCreatedAtDesc(email))
                .thenReturn(List.of(payment));

        List<PaymentResponse> responses =
                paymentService.getMyPayments(email);

        assertEquals(1, responses.size());
        assertEquals("PNR123456", responses.get(0).getPnr());
        assertEquals(PaymentStatus.SUCCESS, responses.get(0).getStatus());
    }

    @Test
    void getMyPayments_EmptyList() {

        when(paymentRepository.findByPassengerEmailOrderByCreatedAtDesc(email))
                .thenReturn(List.of());

        List<PaymentResponse> responses =
                paymentService.getMyPayments(email);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }
}
