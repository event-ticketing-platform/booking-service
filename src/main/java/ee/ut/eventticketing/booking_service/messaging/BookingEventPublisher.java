package ee.ut.eventticketing.booking_service.messaging;

import java.time.LocalDateTime;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ee.ut.eventticketing.booking_service.model.Booking;
import ee.ut.eventticketing.events.BookingCancelledEvent;
import ee.ut.eventticketing.events.BookingConfirmedEvent;

@Component
public class BookingEventPublisher {

    private static final String BOOKING_CONFIRMED_ROUTING_KEY = "BookingConfirmed";
    private static final String BOOKING_CANCELLED_ROUTING_KEY = "BookingCancelled";

    private final RabbitTemplate rabbitTemplate;
    private final String bookingEventsExchange;

    public BookingEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${messaging.exchanges.booking-events}") String bookingEventsExchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.bookingEventsExchange = bookingEventsExchange;
    }

    public void publishBookingConfirmed(Booking booking) {
        rabbitTemplate.convertAndSend(
                bookingEventsExchange,
                BOOKING_CONFIRMED_ROUTING_KEY,
                new BookingConfirmedEvent(
                        booking.getBookingId(),
                        booking.getCustomerId(),
                        booking.getEventId(),
                        booking.getTotalAmount().getAmount(),
                        booking.getTotalAmount().getCurrency(),
                        LocalDateTime.now()));
    }

    public void publishBookingCancelled(Booking booking) {
        rabbitTemplate.convertAndSend(
                bookingEventsExchange,
                BOOKING_CANCELLED_ROUTING_KEY,
                new BookingCancelledEvent(
                        booking.getBookingId(),
                        booking.getCustomerId(),
                        booking.getEventId(),
                        booking.getTotalAmount().getAmount(),
                        booking.getTotalAmount().getCurrency(),
                        LocalDateTime.now()));
    }
}
