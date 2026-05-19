package ee.ut.eventticketing.booking_service.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import ee.ut.eventticketing.booking_service.service.BookingService;
import ee.ut.eventticketing.events.PaymentCompletedEvent;

@Component
public class PaymentCompletedListener {

    private final BookingService bookingService;

    public PaymentCompletedListener(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @RabbitListener(queues = "${messaging.queues.payment-completed}")
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        bookingService.confirmBookingFromPayment(event.bookingId(), event.paymentId());
    }
}
