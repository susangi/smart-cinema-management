package com.example.cinema_management.report_revenue.dto;

import java.time.LocalDate;

public record RevenueReportParams(
        String basis,            
        LocalDate startDate,
        LocalDate endDate,
        Long movieId,           
        Long showTimeId,        
        String paymentMethod,   
        String format            
) {

}
