package ee.ut.eventticketing.booking_service.client;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonAlias;

@Component
public class TicketingClient {

    private final RestClient restClient;

    public TicketingClient(
            RestClient.Builder restClientBuilder,
            @Value("${services.ticketing.base-url}") String ticketingBaseUrl) {
        this.restClient = restClientBuilder.baseUrl(ticketingBaseUrl).build();
    }

    public void reserveTickets(String ticketTypeId, int quantity) {
        restClient.patch()
                .uri("/ticket-types/{ticketTypeId}/reserve", ticketTypeId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new TicketQuantityRequest(quantity))
                .retrieve()
                .toBodilessEntity();
    }

    public void releaseTickets(String ticketTypeId, int quantity) {
        restClient.patch()
                .uri("/ticket-types/{ticketTypeId}/release", ticketTypeId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new TicketQuantityRequest(quantity))
                .retrieve()
                .toBodilessEntity();
    }

    public TicketingTicketResponse issueTicket(String ticketTypeId, Long bookingId) {
        if (ticketTypeId == null || ticketTypeId.isBlank()) {
            throw new IllegalArgumentException("Ticketing ticket type id is required to issue a ticket");
        }
        return restClient.post()
                .uri("/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new IssueTicketRequest(UUID.fromString(ticketTypeId), numericIdToUuid(bookingId)))
                .retrieve()
                .body(TicketingTicketResponse.class);
    }

    public TicketingTicketStatusResponse getTicketStatus(String ticketId) {
        return restClient.get()
                .uri("/tickets/{ticketId}/status", ticketId)
                .retrieve()
                .body(TicketingTicketStatusResponse.class);
    }

    private UUID numericIdToUuid(Long id) {
        return new UUID(0L, id);
    }

    private record TicketQuantityRequest(int quantity) {
    }

    private record IssueTicketRequest(UUID ticketTypeId, UUID bookingId) {
    }

    public record TicketingTicketResponse(
            UUID ticketId,
            UUID ticketTypeId,
            UUID bookingId,
            String qrCode,
            LocalDateTime issuedAt,
            @JsonAlias("isUsed")
            boolean used) {
    }

    public record TicketingTicketStatusResponse(
            UUID ticketId,
            UUID bookingId,
            @JsonAlias("isUsed")
            boolean used) {
    }
}
