package ee.ut.eventticketing.booking_service.dto;

import java.math.BigDecimal;

public record TicketTypeResponse(
        Long ticketTypeId,
        Long eventId,
        String name,
        BigDecimal price,
        String currency,
        int availableQuantity) {
}
