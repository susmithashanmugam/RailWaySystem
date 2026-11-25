
package com.railway.payment.client;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class RazorpayClient {

    private final RestClient restClient;
    private final String keyId;
    private final String keySecret;

    public RazorpayClient(
            @Value("${razorpay.base-url}") String baseUrl,
            @Value("${razorpay.key-id}") String keyId,
            @Value("${razorpay.key-secret}") String keySecret) {

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();

        this.keyId = keyId;
        this.keySecret = keySecret;
    }

    public Map<String, Object> createPaymentLink(
            long amountInPaise,
            String referenceId,
            String description) {

        Map<String, Object> request = new HashMap<>();

        request.put("amount", amountInPaise);
        request.put("currency", "INR");
        request.put("accept_partial", false);
        request.put("reference_id", referenceId);
        request.put("description", description);
        request.put("reminder_enable", false);

        try {
            return restClient.post()
                    .uri("/payment_links")
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers ->
                            headers.setBasicAuth(keyId, keySecret))
                    .body(request)
                    .retrieve()
                    .body(Map.class);

        } catch (RestClientException ex) {
            throw new RuntimeException(
                    "Unable to create Razorpay payment link", ex);
        }
    }
}
