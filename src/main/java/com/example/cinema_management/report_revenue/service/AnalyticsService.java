package com.example.cinema_management.report_revenue.service;

import com.example.cinema_management.report_revenue.dto.RevenueReportDTO;
import com.example.cinema_management.report_revenue.dto.RevenueRow;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {
    public List<RevenueReportDTO.Point> buildNetTrend(List<RevenueRow> rows) {
        return rows.stream()
                .collect(Collectors.groupingBy(RevenueRow::basisDate,
                        Collectors.mapping(RevenueRow::netRevenue,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new RevenueReportDTO.Point(e.getKey(), e.getValue()))
                .toList();
    }
}

