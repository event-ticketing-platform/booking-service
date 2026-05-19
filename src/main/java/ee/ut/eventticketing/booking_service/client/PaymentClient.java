package ee.ut.eventticketing.booking_service.client;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PaymentClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String paymentServiceBaseUrl;

    public PaymentClient(@Value("${services.payment.base-url}") String paymentServiceBaseUrl) {
        this.paymentServiceBaseUrl = paymentServiceBaseUrl;
    }

    public Long createPayment(Long bookingId, BigDecimal amount, String currency) {
        Map<String, Object> request = Map.of(
                "bookingId", bookingId,
                "amount", amount,
                "currency", currency,
                "paymentMethod", "STRIPE");

        PaymentResponse response = restTemplate.postForObject(
                paymentServiceBaseUrl + "/payments",
                new HttpEntity<>(request, authorizationHeaders()),
                PaymentResponse.class);

        if (response == null || response.paymentId() == null) {
            throw new IllegalStateException("Payment service did not return a payment id");
        }

        return response.paymentId();
    }

    private record PaymentResponse(Long paymentId) {
    }

    private HttpHeaders authorizationHeaders() {
        HttpHeaders headers = new HttpHeaders();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            headers.setBearerAuth(jwtAuthentication.getToken().getTokenValue());
        }
        return headers;
    }
}
