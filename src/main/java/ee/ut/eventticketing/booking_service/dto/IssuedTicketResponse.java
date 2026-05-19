package ee.ut.eventticketing.booking_service.dto;

import java.time.LocalDateTime;

public record IssuedTicketResponse(
        Long issuedTicketId,
        Long bookingId,
        Long bookingItemId,
        String ticketTypeId,
        String ticketId,
        String qrCode,
        LocalDateTime issuedAt,
        boolean used) {
}
