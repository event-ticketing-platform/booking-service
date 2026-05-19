package ee.ut.eventticketing.booking_service.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ee.ut.eventticketing.booking_service.dto.EventResponse;
import ee.ut.eventticketing.booking_service.dto.TicketTypeResponse;
import ee.ut.eventticketing.booking_service.service.TicketCatalogService;

@RestController
@RequestMapping
public class TicketCatalogController {

    private final TicketCatalogService ticketCatalogService;

    public TicketCatalogController(TicketCatalogService ticketCatalogService) {
        this.ticketCatalogService = ticketCatalogService;
    }

    @GetMapping("/events")
    public List<EventResponse> listEvents() {
        return ticketCatalogService.listEvents();
    }

    @GetMapping("/events/{eventId}")
    public EventResponse getEvent(@PathVariable Long eventId) {
        return ticketCatalogService.getEvent(eventId);
    }

    @GetMapping({"/events/{eventId}/ticket-types", "/events/{eventId}/tickettypes"})
    public List<TicketTypeResponse> getTicketTypes(@PathVariable Long eventId) {
        return ticketCatalogService.getTicketTypes(eventId);
    }

    @GetMapping("/ticket-types/{ticketTypeId}")
    public TicketTypeResponse getTicketType(@PathVariable Long ticketTypeId) {
        return ticketCatalogService.getTicketType(ticketTypeId);
    }
}
