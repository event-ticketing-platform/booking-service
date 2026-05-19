package ee.ut.eventticketing.booking_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import ee.ut.eventticketing.booking_service.client.PaymentClient;
import ee.ut.eventticketing.booking_service.client.TicketingClient;
import ee.ut.eventticketing.booking_service.dto.BookingItemRequest;
import ee.ut.eventticketing.booking_service.dto.BookingResponse;
import ee.ut.eventticketing.booking_service.dto.CreateBookingRequest;
import ee.ut.eventticketing.booking_service.messaging.BookingEventPublisher;
import ee.ut.eventticketing.booking_service.model.Booking;
import ee.ut.eventticketing.booking_service.model.BookingItem;
import ee.ut.eventticketing.booking_service.model.BookingStatus;
import ee.ut.eventticketing.booking_service.model.Money;
import ee.ut.eventticketing.booking_service.repository.BookingRepository;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    private static final String TICKET_TYPE_ID = "00000000-0000-4000-8000-000000000005";

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TicketingClient ticketingClient;

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private BookingEventPublisher bookingEventPublisher;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void createBookingReservesTicketsAndSavesPendingBooking() {
        CreateBookingRequest request = new CreateBookingRequest(
                1L,
                10L,
                "EUR",
                List.of(new BookingItemRequest(TICKET_TYPE_ID, 2, BigDecimal.valueOf(25.00))));

        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse response = bookingService.createBooking(request);

        verify(ticketingClient).reserveTickets(TICKET_TYPE_ID, 2);
        verify(bookingRepository).save(any(Booking.class));
        assertThat(response.status()).isEqualTo(BookingStatus.PENDING);
        assertThat(response.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
        assertThat(response.items()).hasSize(1);
        verifyNoInteractions(bookingEventPublisher);
    }

    @Test
    void paymentCompletedEventConfirmsBookingAndPublishesBookingConfirmed() {
        Booking booking = new Booking(
                1L,
                10L,
                "EUR",
                List.of(new BookingItem(TICKET_TYPE_ID, 2, new Money(
                        BigDecimal.valueOf(25.00),
                        "EUR"))));
        ReflectionTestUtils.setField(booking, "bookingId", 100L);

        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        bookingService.confirmBookingFromPayment(100L, 900L);

        verify(bookingRepository).save(any(Booking.class));
        verify(bookingEventPublisher).publishBookingConfirmed(booking);
        assertThat(booking.getBookingStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void paymentCompletedEventIgnoresCancelledBooking() {
        Booking booking = new Booking(
                1L,
                10L,
                "EUR",
                List.of(new BookingItem(TICKET_TYPE_ID, 2, new Money(
                        BigDecimal.valueOf(25.00),
                        "EUR"))));
        ReflectionTestUtils.setField(booking, "bookingId", 100L);
        booking.cancel();

        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));

        bookingService.confirmBookingFromPayment(100L, 900L);

        verify(bookingRepository, never()).save(any(Booking.class));
        verifyNoInteractions(bookingEventPublisher);
        assertThat(booking.getBookingStatus()).isEqualTo(BookingStatus.CANCELLED);
    }
}
