package ee.ut.eventticketing.booking_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ee.ut.eventticketing.booking_service.dto.BookingItemRequest;
import ee.ut.eventticketing.booking_service.dto.BookingItemResponse;
import ee.ut.eventticketing.booking_service.dto.BookingResponse;
import ee.ut.eventticketing.booking_service.dto.CreateBookingRequest;
import ee.ut.eventticketing.booking_service.model.BookingStatus;
import ee.ut.eventticketing.booking_service.service.BookingService;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookingControllerTest {

    private static final String TICKET_TYPE_ID = "00000000-0000-4000-8000-000000000005";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @Test
    void createBookingReturnsCreatedBooking() throws Exception {
        CreateBookingRequest request = new CreateBookingRequest(
                1L,
                10L,
                "EUR",
                List.of(new BookingItemRequest(TICKET_TYPE_ID, 2, BigDecimal.valueOf(25.00))));

        BookingResponse response = new BookingResponse(
                100L,
                1L,
                10L,
                BookingStatus.PENDING,
                LocalDateTime.parse("2026-05-03T12:00:00"),
                LocalDateTime.parse("2026-05-03T12:15:00"),
                BigDecimal.valueOf(50.00),
                "EUR",
                List.of(new BookingItemResponse(200L, TICKET_TYPE_ID, 2, BigDecimal.valueOf(25.00), "EUR")));

        when(bookingService.createBooking(any(CreateBookingRequest.class))).thenReturn(response);

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingId").value(100))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(50.0));
    }

    @Test
    void createBookingRejectsInvalidRequest() throws Exception {
        String invalidRequest = """
                {
                  "customerId": 1,
                  "eventId": 10,
                  "currency": "EUR",
                  "items": []
                }
                """;

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"));
    }

    @Test
    void getBookingsReturnsAllBookings() throws Exception {
        BookingResponse response = new BookingResponse(
                100L,
                1L,
                10L,
                BookingStatus.PENDING,
                LocalDateTime.parse("2026-05-03T12:00:00"),
                LocalDateTime.parse("2026-05-03T12:15:00"),
                BigDecimal.valueOf(50.00),
                "EUR",
                List.of(new BookingItemResponse(200L, TICKET_TYPE_ID, 2, BigDecimal.valueOf(25.00), "EUR")));

        when(bookingService.getAllBookings()).thenReturn(List.of(response));

        mockMvc.perform(get("/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookingId").value(100))
                .andExpect(jsonPath("$[0].items[0].ticketTypeId").value(TICKET_TYPE_ID));
    }
}
