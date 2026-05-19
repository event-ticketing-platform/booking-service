package ee.ut.eventticketing.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentCompletedEvent(
        Long paymentId,
        Long bookingId,
        BigDecimal amount,
        String currency,
        String paymentStatus,
        LocalDateTime occurredAt) {
}
