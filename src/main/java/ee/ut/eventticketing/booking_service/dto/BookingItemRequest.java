package ee.ut.eventticketing.booking_service.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookingItemRequest(
        @NotBlank String ticketTypeId,
        @Min(1) int quantity,
        @NotNull @DecimalMin("0.00") BigDecimal unitPrice) {
}
