package com.example.cinema_management.report_revenue.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RevenueRow(
        LocalDate basisDate,
        Long movieId,
        String movieTitle,
        Long showTimeId,
        String paymentMethod,
        BigDecimal sales,       
        BigDecimal refunds,     
        BigDecimal netRevenue   
) {}
