package com.example.cinema_management.booking.controller;

import com.example.cinema_management.booking.service.BookingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/booking")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/start")
    public String start(@RequestParam("scheduleId") Long scheduleId, Model model) {
        var vm = bookingService.buildStartView(scheduleId);
        model.addAllAttributes(Map.of(
                "scheduleId", vm.getScheduleId(),
                "movieId", vm.getMovieId(),
                "movieTitle", vm.getMovieTitle(),
                "screenName", vm.getScreenName(),
                "showTimeText", vm.getShowTimeText(),
                "adultPrice", vm.getAdultPrice(),
                "childPrice", vm.getChildPrice(),
                "initialTotal", vm.getAdultPrice()
        ));
        return "site/movies/booking-details";
    }

    @PostMapping("/create")
    public String create(
            @RequestParam Long scheduleId,
            @RequestParam int adultCount,
            @RequestParam int childCount,
            @RequestParam String buyerEmail,
            RedirectAttributes ra
    ) {
        var bookingId = bookingService.createPendingBooking(scheduleId, adultCount, childCount, buyerEmail);
        ra.addFlashAttribute("bookingId", bookingId);

        return "redirect:/booking/checkout/" + bookingId;
    }

    @GetMapping("/checkout/{bookingId}")
    public String checkout(@PathVariable Long bookingId, Model model) {
        model.addAttribute("bookingId", bookingId);
        return "site/movies/checkout";
    }

    @PostMapping("/checkout/{bookingId}/pay-online")
    public String payOnline(@PathVariable Long bookingId) {
        bookingService.captureOnlinePayment(bookingId);
        var vm = bookingService.confirmAndBuildVM(bookingId);

        return "redirect:/booking/confirmed/" + bookingId;
    }

    @PostMapping("/checkout/{bookingId}/pay-cash")
    public String payCash(@PathVariable Long bookingId) {
        bookingService.markCashPayment(bookingId);

        return "redirect:/booking/confirmed/" + bookingId;
    }

    @GetMapping("/confirmed/{bookingId}")
    public String confirmed(@PathVariable Long bookingId, Model model) {
        var vm = bookingService.buildConfirmationVM(bookingId);
        model.addAttribute("vm", vm);

        return "site/movies/booking-confirmation";
    }
}
