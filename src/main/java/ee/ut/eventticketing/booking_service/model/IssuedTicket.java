package ee.ut.eventticketing.booking_service.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "issued_tickets")
public class IssuedTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long issuedTicketId;

    @Column(nullable = false)
    private Long bookingId;

    @Column(nullable = false)
    private Long bookingItemId;

    @Column(nullable = false)
    private String ticketTypeId;

    @Column(nullable = false, unique = true)
    private String ticketId;

    @Column(nullable = false)
    private String qrCode;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    private boolean used;

    protected IssuedTicket() {
    }

    public IssuedTicket(
            Long bookingId,
            Long bookingItemId,
            String ticketTypeId,
            String ticketId,
            String qrCode,
            LocalDateTime issuedAt,
            boolean used) {
        this.bookingId = bookingId;
        this.bookingItemId = bookingItemId;
        this.ticketTypeId = ticketTypeId;
        this.ticketId = ticketId;
        this.qrCode = qrCode;
        this.issuedAt = issuedAt;
        this.used = used;
    }

    public Long getIssuedTicketId() {
        return issuedTicketId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public Long getBookingItemId() {
        return bookingItemId;
    }

    public String getTicketTypeId() {
        return ticketTypeId;
    }

    public String getTicketId() {
        return ticketId;
    }

    public String getQrCode() {
        return qrCode;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}
