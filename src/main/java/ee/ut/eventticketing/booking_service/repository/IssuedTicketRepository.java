package ee.ut.eventticketing.booking_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ee.ut.eventticketing.booking_service.model.IssuedTicket;

public interface IssuedTicketRepository extends JpaRepository<IssuedTicket, Long> {

    List<IssuedTicket> findByBookingIdOrderByIssuedTicketIdAsc(Long bookingId);
}
