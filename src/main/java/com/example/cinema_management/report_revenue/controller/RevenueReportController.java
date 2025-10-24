package com.example.cinema_management.report_revenue.controller;

import com.example.cinema_management.movie.repository.MovieRepository;
import com.example.cinema_management.report_revenue.service.RevenueReportService;
import com.example.cinema_management.report_revenue.dto.RevenueReportParams;
import com.example.cinema_management.schedule.repository.ScheduleRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Controller for revenue report generation and CSV download.
 * Handles admin UI rendering and report parameter submission.
 */
@Controller
@RequestMapping("/admin/reports/revenue")
public class RevenueReportController {

    private final RevenueReportService service;
    private final MovieRepository movieRepo;
    private final ScheduleRepository showTimeRepo;

    public RevenueReportController(RevenueReportService service,
                                   MovieRepository movieRepo,
                                   ScheduleRepository showTimeRepo) {
        this.service = service;
        this.movieRepo = movieRepo;
        this.showTimeRepo = showTimeRepo;
    }

    /**
     * Displays the revenue report input form with default parameters.
     */
    @GetMapping
    public String form(Model m) {
        m.addAttribute("movies", movieRepo.findAll());
        m.addAttribute("params", new RevenueReportParams(
                "TRANSACTION_DATE",
                LocalDate.now().minusDays(7),
                LocalDate.now(),
                null, null, null, "SCREEN"));
        return "admin/report_revenue/report-revenue";
    }

    /**
     * Generates a report preview based on form parameters.
     */
    @PostMapping
    public String generate(@ModelAttribute RevenueReportParams params, Model m) {
        var report = service.generate(params);
        m.addAttribute("params", params);
        m.addAttribute("report", report);
        m.addAttribute("movies", movieRepo.findAll());
        return "admin/report_revenue/report-revenue";
    }

    /**
     * Exports a generated revenue report as a downloadable CSV file.
     */
    @PostMapping("/download")
    public ResponseEntity<byte[]> download(@ModelAttribute RevenueReportParams params) {
        var report = service.generate(params);
        byte[] csv = service.toCsv(report);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=revenue_%s_%s.csv".formatted(params.startDate(), params.endDate()))
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}
