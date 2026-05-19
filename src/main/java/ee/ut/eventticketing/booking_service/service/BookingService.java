package ee.ut.eventticketing.booking_service.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ee.ut.eventticketing.booking_service.client.PaymentClient;
import ee.ut.eventticketing.booking_service.client.TicketingClient;
import ee.ut.eventticketing.booking_service.client.TicketingClient.TicketingTicketResponse;
import ee.ut.eventticketing.booking_service.dto.BookingItemRequest;
import ee.ut.eventticketing.booking_service.dto.BookingItemResponse;
import ee.ut.eventticketing.booking_service.dto.BookingResponse;
import ee.ut.eventticketing.booking_service.dto.CreateBookingRequest;
import ee.ut.eventticketing.booking_service.dto.IssuedTicketResponse;
import ee.ut.eventticketing.booking_service.dto.PaymentInitiationResponse;
import ee.ut.eventticketing.booking_service.exception.BookingNotFoundException;
import ee.ut.eventticketing.booking_service.messaging.BookingEventPublisher;
import ee.ut.eventticketing.booking_service.model.Booking;
import ee.ut.eventticketing.booking_service.model.BookingItem;
import ee.ut.eventticketing.booking_service.model.BookingStatus;
import ee.ut.eventticketing.booking_service.model.IssuedTicket;
import ee.ut.eventticketing.booking_service.model.Money;
import ee.ut.eventticketing.booking_service.repository.BookingRepository;
import ee.ut.eventticketing.booking_service.repository.IssuedTicketRepository;

@Service
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TicketingClient ticketingClient;
    private final PaymentClient paymentClient;
    private final BookingEventPublisher bookingEventPublisher;
    private final IssuedTicketRepository issuedTicketRepository;

    public BookingService(
            BookingRepository bookingRepository,
            TicketingClient ticketingClient,
            PaymentClient paymentClient,
            BookingEventPublisher bookingEventPublisher,
            IssuedTicketRepository issuedTicketRepository) {
        this.bookingRepository = bookingRepository;
        this.ticketingClient = ticketingClient;
        this.paymentClient = paymentClient;
        this.bookingEventPublisher = bookingEventPublisher;
        this.issuedTicketRepository = issuedTicketRepository;
    }

    public BookingResponse createBooking(CreateBookingRequest request) {
        request.items().forEach(item -> ticketingClient.reserveTickets(item.ticketTypeId(), item.quantity()));

        List<BookingItem> items = request.items().stream()
                .map(item -> toBookingItem(item, request.currency()))
                .toList();

        Booking booking = new Booking(request.customerId(), request.eventId(), request.currency(), items);
        return toResponse(bookingRepository.save(booking));
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(Long bookingId) {
        return toResponse(findBooking(bookingId));
    }

    public BookingResponse cancelBooking(Long bookingId) {
        Booking booking = findBooking(bookingId);
        booking.cancel();
        booking.getItems().forEach(item -> ticketingClient.releaseTickets(item.getTicketTypeId(), item.getQuantity()));
        Booking saved = bookingRepository.save(booking);
        bookingEventPublisher.publishBookingCancelled(saved);
        return toResponse(saved);
    }

    public PaymentInitiationResponse initiatePayment(Long bookingId) {
        Booking booking = findBooking(bookingId);
        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Only pending bookings can be paid");
        }
        if (booking.isExpired()) {
            throw new IllegalStateException("Booking reservation has expired");
        }

        Long paymentId = paymentClient.createPayment(
                booking.getBookingId(),
                booking.getTotalAmount().getAmount(),
                booking.getTotalAmount().getCurrency());

        return new PaymentInitiationResponse(
                booking.getBookingId(),
                paymentId,
                "Payment initiated for booking " + booking.getBookingId());
    }

    public BookingResponse confirmBooking(Long bookingId) {
        return toResponse(confirmAndPublish(bookingId));
    }

    public void confirmBookingFromPayment(Long bookingId, Long paymentId) {
        Booking booking = findBooking(bookingId);
        if (booking.getBookingStatus() != BookingStatus.PENDING || booking.isExpired()) {
            return;
        }
        confirmAndPublish(booking);
    }

    private Booking confirmAndPublish(Long bookingId) {
        Booking booking = findBooking(bookingId);
        if (booking.getBookingStatus() == BookingStatus.CONFIRMED) {
            return booking;
        }
        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Only pending bookings can be confirmed");
        }
        if (booking.isExpired()) {
            throw new IllegalStateException("Booking reservation has expired");
        }

        return confirmAndPublish(booking);
    }

    private Booking confirmAndPublish(Booking booking) {
        booking.confirm();
        Booking saved = bookingRepository.save(booking);
        bookingEventPublisher.publishBookingConfirmed(saved);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByUser(Long userId) {
        return bookingRepository.findByCustomerId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<IssuedTicketResponse> getOrIssueTickets(Long bookingId) {
        Booking booking = findBooking(bookingId);
        if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Tickets are available after the booking is confirmed");
        }

        List<IssuedTicket> existingTickets = issuedTicketRepository.findByBookingIdOrderByIssuedTicketIdAsc(bookingId);
        int expectedTicketCount = booking.getItems().stream()
                .filter(this::hasTicketingTicketTypeId)
                .mapToInt(BookingItem::getQuantity)
                .sum();

        if (existingTickets.size() < expectedTicketCount) {
            issueMissingTickets(booking, existingTickets);
            existingTickets = issuedTicketRepository.findByBookingIdOrderByIssuedTicketIdAsc(bookingId);
        }

        return existingTickets.stream()
                .map(this::refreshAndMapIssuedTicket)
                .toList();
    }

    private Booking findBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
    }

    private void issueMissingTickets(Booking booking, List<IssuedTicket> existingTickets) {
        for (BookingItem item : booking.getItems()) {
            if (!hasTicketingTicketTypeId(item)) {
                continue;
            }
            long existingForItem = existingTickets.stream()
                    .filter(ticket -> item.getBookingItemId().equals(ticket.getBookingItemId()))
                    .count();
            for (long index = existingForItem; index < item.getQuantity(); index++) {
                TicketingTicketResponse ticket = ticketingClient.issueTicket(item.getTicketTypeId(), booking.getBookingId());
                issuedTicketRepository.save(new IssuedTicket(
                        booking.getBookingId(),
                        item.getBookingItemId(),
                        String.valueOf(ticket.ticketTypeId()),
                        String.valueOf(ticket.ticketId()),
                        ticket.qrCode() != null ? ticket.qrCode() : String.valueOf(ticket.ticketId()),
                        ticket.issuedAt(),
                        ticket.used()));
            }
        }
    }

    private boolean hasTicketingTicketTypeId(BookingItem item) {
        try {
            UUID.fromString(item.getTicketTypeId());
            return true;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private IssuedTicketResponse refreshAndMapIssuedTicket(IssuedTicket ticket) {
        try {
            boolean used = ticketingClient.getTicketStatus(ticket.getTicketId()).used();
            ticket.setUsed(used);
        } catch (RuntimeException ignored) {
            // Keep the locally issued ticket visible even if Ticketing status is temporarily unavailable.
        }
        return new IssuedTicketResponse(
                ticket.getIssuedTicketId(),
                ticket.getBookingId(),
                ticket.getBookingItemId(),
                ticket.getTicketTypeId(),
                ticket.getTicketId(),
                ticket.getQrCode(),
                ticket.getIssuedAt(),
                ticket.isUsed());
    }

    private BookingItem toBookingItem(BookingItemRequest request, String currency) {
        if (request.ticketTypeId().isBlank()) {
            throw new IllegalArgumentException("Ticket type is required");
        }
        return new BookingItem(
                request.ticketTypeId(),
                request.quantity(),
                new Money(request.unitPrice(), currency));
    }

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getBookingId(),
                booking.getCustomerId(),
                booking.getEventId(),
                booking.getBookingStatus(),
                booking.getReservationTime(),
                booking.getReservationExpiry(),
                booking.getTotalAmount().getAmount(),
                booking.getTotalAmount().getCurrency(),
                booking.getItems().stream().map(this::toItemResponse).toList());
    }

    private BookingItemResponse toItemResponse(BookingItem item) {
        return new BookingItemResponse(
                item.getBookingItemId(),
                item.getTicketTypeId(),
                item.getQuantity(),
                item.getUnitPrice().getAmount(),
                item.getUnitPrice().getCurrency());
    }
}
