package ee.ut.eventticketing.booking_service.model;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
public class TicketType {

    @Id
    private Long ticketTypeId;

    private Long eventId;
    private String name;
    private BigDecimal price;
    private String currency;
    private int availableQuantity;

    @Version
    private Long version;

    protected TicketType() {
    }

    public TicketType(Long ticketTypeId, Long eventId, String name, BigDecimal price, String currency, int availableQuantity) {
        this.ticketTypeId = ticketTypeId;
        this.eventId = eventId;
        this.name = name;
        this.price = price;
        this.currency = currency;
        this.availableQuantity = availableQuantity;
    }

    public void reserve(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        if (availableQuantity < quantity) {
            throw new IllegalStateException("Not enough tickets available");
        }
        availableQuantity -= quantity;
    }

    public void release(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        availableQuantity += quantity;
    }

    public Long getTicketTypeId() {
        return ticketTypeId;
    }

    public Long getEventId() {
        return eventId;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }
}
