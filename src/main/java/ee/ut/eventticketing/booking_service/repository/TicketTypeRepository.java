package ee.ut.eventticketing.booking_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ee.ut.eventticketing.booking_service.model.TicketType;

public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {

    List<TicketType> findByEventIdOrderByTicketTypeIdAsc(Long eventId);
}
