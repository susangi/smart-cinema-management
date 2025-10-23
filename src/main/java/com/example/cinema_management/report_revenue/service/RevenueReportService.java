package com.example.cinema_management.report_revenue.service;

import com.example.cinema_management.report_revenue.dto.RevenueReportDTO;
import com.example.cinema_management.report_revenue.dto.RevenueReportParams;
import com.example.cinema_management.report_revenue.repository.RevenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collector;

@Service
public class RevenueReportService {
    private final RevenueRepository revenueRepo;
    private final AnalyticsService analytics;

    public RevenueReportService(RevenueRepository revenueRepo, AnalyticsService analytics) {
        this.revenueRepo = revenueRepo;
        this.analytics = analytics;
    }

    public RevenueReportDTO generate(RevenueReportParams params) {
        var rows = revenueRepo.findRevenue(params);
        var totals = rows.stream().collect(
                Collector.of(
                        () -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO},
                        (acc, r) -> { acc[0] = acc[0].add(r.sales()); acc[1] = acc[1].add(r.refunds()); },
                        (a,b) -> new BigDecimal[]{ a[0].add(b[0]), a[1].add(b[1]) }
                )
        );
        var trend = analytics.buildNetTrend(rows);
        return new RevenueReportDTO(
                params, rows,
                totals[0], totals[1], totals[0].subtract(totals[1]),
                trend
        );
    }

    public byte[] toCsv(RevenueReportDTO dto) {
        var sb = new StringBuilder("Date,Movie,ShowTimeId,Method,Sales,Refunds,Net\n");
        dto.rows().forEach(r -> sb.append(String.join(",",
                r.basisDate().toString(),
                quote(r.movieTitle()),
                String.valueOf(r.showTimeId()),
                r.paymentMethod()==null? "" : r.paymentMethod(),
                r.sales().toPlainString(),
                r.refunds().toPlainString(),
                r.netRevenue().toPlainString()
        )).append("\n"));
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
    private static String quote(String s){ return "\"" + (s==null?"":s.replace("\"","\"\"")) + "\""; }
}

