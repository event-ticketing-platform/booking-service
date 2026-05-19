package ee.ut.eventticketing.booking_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ee.ut.eventticketing.booking_service.model.EventListing;

public interface EventListingRepository extends JpaRepository<EventListing, Long> {

    List<EventListing> findByActiveTrueOrderByStartsAtAsc();
}
