package ee.ut.eventticketing.booking_service.dto;

import java.time.LocalDateTime;
import java.util.List;

public record EventResponse(
        Long eventId,
        String title,
        String venue,
        String city,
        LocalDateTime startsAt,
        String category,
        String status,
        String image,
        List<TicketTypeResponse> tickets) {
}
