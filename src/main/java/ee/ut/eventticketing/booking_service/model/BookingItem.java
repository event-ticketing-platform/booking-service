package ee.ut.eventticketing.booking_service.model;

import jakarta.persistence.Embedded;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class BookingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingItemId;

    @Column(name = "external_ticket_type_id")
    private String ticketTypeId;

    private int quantity;

    @Embedded
    private Money unitPrice;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    protected BookingItem() {
    }

    public BookingItem(String ticketTypeId, int quantity, Money unitPrice) {
        this.ticketTypeId = ticketTypeId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    void assignToBooking(Booking booking) {
        this.booking = booking;
    }

    public Long getBookingItemId() {
        return bookingItemId;
    }

    public String getTicketTypeId() {
        return ticketTypeId;
    }

    public int getQuantity() {
        return quantity;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }
}
