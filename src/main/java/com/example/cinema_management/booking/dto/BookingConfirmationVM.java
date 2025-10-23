package com.example.cinema_management.booking.dto;

import java.math.BigDecimal;
import java.util.List;

public record BookingConfirmationVM(
        Long bookingId,
        String movieTitle,
        String screenName,
        String showTimeText,
        BigDecimal total,
        List<TicketVM> tickets
) {
    public record TicketVM(Long ticketId, String label, String qrBase64) {}
}
