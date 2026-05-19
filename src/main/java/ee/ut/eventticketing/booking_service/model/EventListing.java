package ee.ut.eventticketing.booking_service.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class EventListing {

    @Id
    private Long eventId;

    private String title;
    private String venue;
    private String city;
    private LocalDateTime startsAt;
    private String category;
    private String status;

    @Column(length = 1024)
    private String image;
    private boolean active;

    protected EventListing() {
    }

    public EventListing(
            Long eventId,
            String title,
            String venue,
            String city,
            LocalDateTime startsAt,
            String category,
            String status,
            String image) {
        this.eventId = eventId;
        this.title = title;
        this.venue = venue;
        this.city = city;
        this.startsAt = startsAt;
        this.category = category;
        this.status = status;
        this.image = image;
        this.active = true;
    }

    public Long getEventId() {
        return eventId;
    }

    public String getTitle() {
        return title;
    }

    public String getVenue() {
        return venue;
    }

    public String getCity() {
        return city;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public String getCategory() {
        return category;
    }

    public String getStatus() {
        return status;
    }

    public String getImage() {
        return image;
    }

    public boolean isActive() {
        return active;
    }
}
