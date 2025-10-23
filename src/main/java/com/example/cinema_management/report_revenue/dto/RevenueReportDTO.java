package com.example.cinema_management.report_revenue.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RevenueReportDTO(
        RevenueReportParams params,
        List<RevenueRow> rows,
        BigDecimal totalSales,
        BigDecimal totalRefunds,
        BigDecimal totalNet,
        List<Point> trend       
) {
    public record Point(LocalDate date, BigDecimal net) {}
}
