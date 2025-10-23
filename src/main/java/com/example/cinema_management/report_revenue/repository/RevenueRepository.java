package com.example.cinema_management.report_revenue.repository;

import com.example.cinema_management.report_revenue.dto.RevenueReportParams;
import com.example.cinema_management.report_revenue.dto.RevenueRow;

import java.util.List;

public interface RevenueRepository {
    
    List<RevenueRow> findRevenue(RevenueReportParams p);
}
