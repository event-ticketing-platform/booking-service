package ee.ut.eventticketing.booking_service.config;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import ee.ut.eventticketing.booking_service.model.EventListing;
import ee.ut.eventticketing.booking_service.model.TicketType;
import ee.ut.eventticketing.booking_service.repository.EventListingRepository;
import ee.ut.eventticketing.booking_service.repository.TicketTypeRepository;

@Component
public class TicketCatalogDataInitializer implements ApplicationRunner {

    private final EventListingRepository eventListingRepository;
    private final TicketTypeRepository ticketTypeRepository;

    public TicketCatalogDataInitializer(
            EventListingRepository eventListingRepository,
            TicketTypeRepository ticketTypeRepository) {
        this.eventListingRepository = eventListingRepository;
        this.ticketTypeRepository = ticketTypeRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (eventListingRepository.count() > 0) {
            return;
        }

        eventListingRepository.saveAll(List.of(
                new EventListing(
                        201L,
                        "Northern Lights Music Festival",
                        "Tallinn Creative Hub",
                        "Tallinn",
                        LocalDateTime.of(2026, 5, 22, 19, 30),
                        "Festival",
                        "Selling fast",
                        "linear-gradient(135deg, rgba(17, 24, 39, 0.15), rgba(17, 24, 39, 0.85)), radial-gradient(circle at 22% 20%, #f7c948 0 9%, transparent 10%), radial-gradient(circle at 78% 18%, #58c4dd 0 8%, transparent 9%), linear-gradient(145deg, #17324d, #0e1726 65%, #251a31)"),
                new EventListing(
                        202L,
                        "Startup Pitch Night",
                        "Delta Centre Auditorium",
                        "Tartu",
                        LocalDateTime.of(2026, 5, 26, 18, 0),
                        "Business",
                        "Open",
                        "linear-gradient(135deg, rgba(15, 23, 42, 0.1), rgba(15, 23, 42, 0.84)), repeating-linear-gradient(90deg, rgba(255,255,255,.16) 0 1px, transparent 1px 54px), linear-gradient(145deg, #195a6a, #102b3f 55%, #0f172a)"),
                new EventListing(
                        203L,
                        "Spring Theatre Premiere",
                        "Vanemuine Small House",
                        "Tartu",
                        LocalDateTime.of(2026, 5, 30, 20, 0),
                        "Culture",
                        "Few seats left",
                        "linear-gradient(135deg, rgba(26, 20, 35, 0.08), rgba(26, 20, 35, 0.88)), radial-gradient(circle at 70% 26%, #f2b84b 0 10%, transparent 11%), linear-gradient(145deg, #442250, #201626 58%, #111827)")));

        ticketTypeRepository.saveAll(List.of(
                new TicketType(501L, 201L, "Floor access", BigDecimal.valueOf(49), "EUR", 128),
                new TicketType(502L, 201L, "Balcony seat", BigDecimal.valueOf(36), "EUR", 74),
                new TicketType(503L, 201L, "Student ticket", BigDecimal.valueOf(24), "EUR", 41),
                new TicketType(601L, 202L, "General admission", BigDecimal.valueOf(18), "EUR", 220),
                new TicketType(602L, 202L, "Founder table", BigDecimal.valueOf(79), "EUR", 18),
                new TicketType(701L, 203L, "Stalls", BigDecimal.valueOf(42), "EUR", 16),
                new TicketType(702L, 203L, "Upper circle", BigDecimal.valueOf(29), "EUR", 33)));
    }
}
