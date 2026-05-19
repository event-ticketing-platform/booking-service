package ee.ut.eventticketing.booking_service.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ee.ut.eventticketing.booking_service.dto.EventResponse;
import ee.ut.eventticketing.booking_service.dto.TicketTypeResponse;
import ee.ut.eventticketing.booking_service.model.EventListing;
import ee.ut.eventticketing.booking_service.model.TicketType;
import ee.ut.eventticketing.booking_service.repository.EventListingRepository;
import ee.ut.eventticketing.booking_service.repository.TicketTypeRepository;

@Service
@Transactional
public class TicketCatalogService {

    private final EventListingRepository eventListingRepository;
    private final TicketTypeRepository ticketTypeRepository;

    public TicketCatalogService(
            EventListingRepository eventListingRepository,
            TicketTypeRepository ticketTypeRepository) {
        this.eventListingRepository = eventListingRepository;
        this.ticketTypeRepository = ticketTypeRepository;
    }

    @Transactional(readOnly = true)
    public List<EventResponse> listEvents() {
        return eventListingRepository.findByActiveTrueOrderByStartsAtAsc().stream()
                .map(this::toEventResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EventResponse getEvent(Long eventId) {
        EventListing event = eventListingRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event " + eventId + " was not found"));
        return toEventResponse(event);
    }

    @Transactional(readOnly = true)
    public List<TicketTypeResponse> getTicketTypes(Long eventId) {
        return ticketTypeRepository.findByEventIdOrderByTicketTypeIdAsc(eventId).stream()
                .map(this::toTicketTypeResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TicketTypeResponse getTicketType(Long ticketTypeId) {
        return toTicketTypeResponse(findTicketType(ticketTypeId));
    }

    public void reserveTickets(Long ticketTypeId, int quantity) {
        TicketType ticketType = findTicketType(ticketTypeId);
        ticketType.reserve(quantity);
        ticketTypeRepository.save(ticketType);
    }

    public void releaseTickets(Long ticketTypeId, int quantity) {
        TicketType ticketType = findTicketType(ticketTypeId);
        ticketType.release(quantity);
        ticketTypeRepository.save(ticketType);
    }

    private EventResponse toEventResponse(EventListing event) {
        return new EventResponse(
                event.getEventId(),
                event.getTitle(),
                event.getVenue(),
                event.getCity(),
                event.getStartsAt(),
                event.getCategory(),
                event.getStatus(),
                event.getImage(),
                getTicketTypes(event.getEventId()));
    }

    private TicketTypeResponse toTicketTypeResponse(TicketType ticketType) {
        return new TicketTypeResponse(
                ticketType.getTicketTypeId(),
                ticketType.getEventId(),
                ticketType.getName(),
                ticketType.getPrice(),
                ticketType.getCurrency(),
                ticketType.getAvailableQuantity());
    }

    private TicketType findTicketType(Long ticketTypeId) {
        return ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket type " + ticketTypeId + " was not found"));
    }
}
