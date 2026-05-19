package ee.ut.eventticketing.booking_service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ee.ut.eventticketing.booking_service.dto.BookingResponse;
import ee.ut.eventticketing.booking_service.dto.CreateBookingRequest;
import ee.ut.eventticketing.booking_service.dto.IssuedTicketResponse;
import ee.ut.eventticketing.booking_service.dto.PaymentInitiationResponse;
import ee.ut.eventticketing.booking_service.service.BookingService;
import jakarta.validation.Valid;

@RestController
@RequestMapping
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public BookingResponse createBooking(@Valid @RequestBody CreateBookingRequest request) {
        return bookingService.createBooking(request);
    }

    @GetMapping("/bookings")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public List<BookingResponse> getBookings() {
        return bookingService.getAllBookings();
    }

    @GetMapping("/bookings/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public BookingResponse getBooking(@PathVariable Long id) {
        return bookingService.getBooking(id);
    }

    @GetMapping("/bookings/{id}/tickets")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public List<IssuedTicketResponse> getBookingTickets(@PathVariable Long id) {
        return bookingService.getOrIssueTickets(id);
    }

    @DeleteMapping("/bookings/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public BookingResponse cancelBooking(@PathVariable Long id) {
        return bookingService.cancelBooking(id);
    }

    @PostMapping("/bookings/{id}/pay")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public PaymentInitiationResponse initiatePayment(@PathVariable Long id) {
        return bookingService.initiatePayment(id);
    }

    @PostMapping("/bookings/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public BookingResponse confirmBooking(@PathVariable Long id) {
        return bookingService.confirmBooking(id);
    }

    @GetMapping("/users/{userId}/bookings")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public List<BookingResponse> getBookingsByUser(@PathVariable Long userId) {
        return bookingService.getBookingsByUser(userId);
    }

}
