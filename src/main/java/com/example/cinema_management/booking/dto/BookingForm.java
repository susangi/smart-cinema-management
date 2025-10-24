package com.example.cinema_management.booking.dto;

import com.example.cinema_management.payment.entity.PaymentMethod;

public record BookingForm(
        Long scheduleId,
        Integer adultCount,
        Integer childCount,
        String buyerEmail,
        PaymentMethod paymentMethod
) {}