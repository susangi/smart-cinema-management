package com.example.cinema_management.booking.controller;

import com.example.cinema_management.booking.dto.BookingForm;
import com.example.cinema_management.booking.service.BookingService;
import com.example.cinema_management.movie.repository.MovieRepository;
import com.example.cinema_management.payment.entity.PaymentMethod;
import com.example.cinema_management.schedule.repository.ScheduleRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/booking")
public class BookingController {
    private final MovieRepository movieRepo;
    private final ScheduleRepository scheduleRepo;
    private final BookingService bookingService;

    public BookingController(MovieRepository movieRepo, ScheduleRepository scheduleRepo, BookingService bookingService) {
        this.movieRepo = movieRepo;
        this.scheduleRepo = scheduleRepo;
        this.bookingService = bookingService;
    }

    @GetMapping("/")
    public String bookStart(Model m) {
        m.addAttribute("movies", movieRepo.findAll());
        m.addAttribute("form", new BookingForm(null, null, 1, 0, "", PaymentMethod.ONLINE));
        return "web/book-start";
    }

    @GetMapping("/schedule")
    public String chooseSchedule(@RequestParam Long movieId, Model m) {
        m.addAttribute("movies", movieRepo.findAll());
        m.addAttribute("selectedMovieId", movieId);
        m.addAttribute("schedules", scheduleRepo.findByMovieId(movieId));
        m.addAttribute("form", new BookingForm(movieId, null, 1, 0, "", PaymentMethod.ONLINE));
        return "web/book-start";
    }

    @PostMapping("/")
    public String create(@ModelAttribute BookingForm form, RedirectAttributes ra, Principal principal) {
        Long userId = (principal == null ? null : Long.valueOf(1)); // map to your user id
        Long id = bookingService.startBooking(form, userId);
        ra.addAttribute("id", id);
        return "redirect:/booking/checkout/{id}";
    }

    @GetMapping("/checkout/{id}")
    public String checkout(@PathVariable Long id, Model m) {
        m.addAttribute("bookingId", id);
        return "web/checkout";
    }

    @PostMapping("/checkout/{id}/pay-online")
    public String payOnline(@PathVariable Long id, RedirectAttributes ra) {
        ra.addAttribute("id", id);
        return "redirect:/booking/bookings/{id}/confirmed";
    }
    @PostMapping("/checkout/{id}/pay-cash")
    public String payCash(@PathVariable Long id, RedirectAttributes ra) {
        ra.addAttribute("id", id);
        return "redirect:/booking/bookings/{id}/confirmed";
    }

    @GetMapping("/bookings/{id}/confirmed")
    public String confirmed(@PathVariable Long id, Model m) {
        var vm = bookingService.simulatePaymentAndIssue(id);
        m.addAttribute("vm", vm);
        return "web/booking-confirmation";
    }
}

