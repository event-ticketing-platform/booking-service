package ee.ut.eventticketing.booking_service.dto;

import java.math.BigDecimal;

public record BookingItemResponse(
        Long bookingItemId,
        String ticketTypeId,
        int quantity,
        BigDecimal unitPrice,
        String currency) {
}
