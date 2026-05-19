package ee.ut.eventticketing.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingConfirmedEvent(
        Long bookingId,
        Long customerId,
        Long eventId,
        BigDecimal totalAmount,
        String currency,
        LocalDateTime occurredAt) {
}
