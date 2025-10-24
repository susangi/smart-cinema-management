package com.example.cinema_management.booking.service;

import com.example.cinema_management.booking.dto.BookingConfirmationVM;
import com.example.cinema_management.booking.dto.BookingForm;
import com.example.cinema_management.booking.dto.BookingStartVM;
import com.example.cinema_management.booking.entity.Booking;
import com.example.cinema_management.booking.entity.BookingStatus;
import com.example.cinema_management.booking.repository.BookingRepository;
import com.example.cinema_management.movie.entity.Movie;
import com.example.cinema_management.movie.repository.MovieRepository;
import com.example.cinema_management.payment.entity.Payment;
import com.example.cinema_management.payment.entity.PaymentMethod;
import com.example.cinema_management.payment.entity.PaymentStatus;
import com.example.cinema_management.payment.repository.PaymentRepository;
import com.example.cinema_management.pricing.PricingService;
import com.example.cinema_management.schedule.entity.Schedule;
import com.example.cinema_management.schedule.repository.ScheduleRepository;
import com.example.cinema_management.screen.entity.Screen;
import com.example.cinema_management.ticket.entity.Ticket;
import com.example.cinema_management.ticket.entity.TicketStatus;
import com.example.cinema_management.ticket.repository.TicketRepository;
import com.example.cinema_management.utility.MailService;
import com.example.cinema_management.utility.QrGenerator;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class BookingService {
    private final MovieRepository movieRepo;
    private final ScheduleRepository scheduleRepo;
    private final PricingService pricingService;
    private final BookingRepository bookingRepo;
    private final PaymentRepository paymentRepo;
    private final TicketRepository ticketRepo;
    private final QrGenerator qr;
    private final MailService mail;

    public BookingService(MovieRepository movieRepo, ScheduleRepository scheduleRepo, PricingService pricingService, BookingRepository bookingRepo, PaymentRepository paymentRepo, TicketRepository ticketRepo, QrGenerator qr, MailService mail) {
        this.movieRepo = movieRepo;
        this.scheduleRepo = scheduleRepo;
        this.pricingService = pricingService;
        this.bookingRepo = bookingRepo;
        this.paymentRepo = paymentRepo;
        this.ticketRepo = ticketRepo;
        this.qr = qr;
        this.mail = mail;
    }

    // New method to build the start view
    public BookingStartVM buildStartView(Long scheduleId) {
        var schedule = scheduleRepo.findById(scheduleId).orElseThrow();
        var movie = schedule.getMovie();
        var screen = schedule.getScreen();

        var adultPrice = pricingService.computePrice(scheduleId, true);
        var childPrice = pricingService.computePrice(scheduleId, false);

        var showTimeText = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm")
                .format(schedule.getSessionStartTime());

        return new BookingStartVM(
                scheduleId,
                movie.getId(),
                movie.getTitle(),
                screen.getName(),
                showTimeText,
                adultPrice,
                childPrice
        );
    }

    @Transactional
    public Long createPendingBooking(Long scheduleId, Integer adultCount, Integer childCount, String buyerEmail) {
        var form = new BookingForm(scheduleId, adultCount, childCount, buyerEmail, PaymentMethod.ONLINE);
        return startBooking(form, null);
    }

    @Transactional
    public Long startBooking(BookingForm form, @Nullable Long currentUserId) {
        var schedule = scheduleRepo.findById(form.scheduleId()).orElseThrow();

        int adults = Math.max(0, form.adultCount() == null ? 0 : form.adultCount());
        int children = Math.max(0, form.childCount() == null ? 0 : form.childCount());
        if (adults + children <= 0) throw new IllegalArgumentException("Select at least 1 seat");

        var total = pricingService.computeTotal(form.scheduleId(), adults, children);

        var booking = new Booking();
        booking.setUserId(currentUserId);
        booking.setBuyerEmail(form.buyerEmail());
        booking.setSchedule(schedule);
        booking.setTotalPrice(total);
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setAdultCount(adults);
        booking.setChildCount(children);
        booking = bookingRepo.save(booking);

        var pay = new Payment();
        pay.setBooking(booking);
        pay.setMethod(form.paymentMethod());
        pay.setAmount(total);
        pay.setStatus(PaymentStatus.PENDING);
        pay.setCreatedAt(LocalDateTime.now());
        paymentRepo.save(pay);

        return booking.getId();
    }

    // New method to capture online payment
    @Transactional
    public void captureOnlinePayment(Long bookingId) {
        // In a real application, this would integrate with a payment gateway
        // For simulation, we just mark it as successful
        var payment = paymentRepo.findByBookingId(bookingId).orElseThrow();
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepo.save(payment);
    }

    @Transactional
    public void markCashPayment(Long bookingId) {
        var payment = paymentRepo.findByBookingId(bookingId).orElseThrow();
        payment.setMethod(PaymentMethod.CASH);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepo.save(payment);
    }

    // New method to confirm and build VM
    @Transactional
    public BookingConfirmationVM confirmAndBuildVM(Long bookingId) {
        var booking = bookingRepo.findById(bookingId).orElseThrow();
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepo.save(booking);

        return simulatePaymentAndIssue(bookingId);
    }

    // New method to build confirmation VM
    public BookingConfirmationVM buildConfirmationVM(Long bookingId) {
        var booking = bookingRepo.findById(bookingId).orElseThrow();
        var schedule = booking.getSchedule();

        var showTimeText = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm")
                .format(schedule.getSessionStartTime());

        var tickets = ticketRepo.findByBookingId(bookingId);
        var ticketVMs = tickets.stream()
                .map(ticket -> {
                    var payload = "TICKET:" + ticket.getId() + ";SCHEDULE:" + schedule.getId();
                    String base64 = qr.pngBase64(payload, 256);
                    return new BookingConfirmationVM.TicketVM(ticket.getId(), ticket.getSeatNumber(), base64);
                })
                .collect(Collectors.toList());

        return new BookingConfirmationVM(
                booking.getId(),
                movieTitle(schedule.getMovie().getId()),
                screenName(schedule.getScreen().getId()),
                showTimeText,
                booking.getTotalPrice(),
                ticketVMs
        );
    }

    @Transactional
    public BookingConfirmationVM simulatePaymentAndIssue(Long bookingId) {
        var booking = bookingRepo.findById(bookingId).orElseThrow();

        var payment = paymentRepo.findByBookingId(bookingId).orElseThrow();
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());

        var ticketsVM = new ArrayList<BookingConfirmationVM.TicketVM>();
        int totalQty = booking.getAdultCount() + booking.getChildCount();
        for (int i = 1; i <= totalQty; i++) {
            var ticket = new Ticket();
            ticket.setBooking(booking);
            ticket.setQrCode(UUID.randomUUID().toString());

            ticket.setStatus(TicketStatus.NEW);
            ticket.setSeatNumber(String.valueOf(i));
            ticket.setCreatedAt(LocalDateTime.now());
            ticket = ticketRepo.save(ticket);

            // keep your current QR payload format:
            var payload = "TICKET:" + ticket.getId() + ";SCHEDULE:" + booking.getSchedule().getId();
            String base64 = qr.pngBase64(payload, 256);
            ticketsVM.add(new BookingConfirmationVM.TicketVM(ticket.getId(), ticket.getSeatNumber(), base64));
        }

        booking.setStatus(BookingStatus.CONFIRMED);

        if (booking.getBuyerEmail() != null && !booking.getBuyerEmail().isBlank()) {
            var pngs = ticketsVM.stream()
                    .map(t -> Base64.getDecoder().decode(t.qrBase64()))
                    .toList();
            String html = """
                <p>Booking confirmed!</p>
                <p>Movie %s!</p>
                <p>Showtime: %s</p>
                <p>Total: %s</p>
                <p>QRs attached below:</p>
                %s
            """.formatted(
                    booking.getSchedule().getMovie().getTitle(),
                    booking.getSchedule().getSessionStartTime().toString(),
                    booking.getTotalPrice().toPlainString(),
                    IntStream.range(0, pngs.size())
                            .mapToObj(i -> "<img style='width:160px;height:160px;margin:6px' src='cid:qr"+i+"'/>")
                            .collect(Collectors.joining())
            );
            mail.sendQrEmail(booking.getBuyerEmail(), "Your Cinema Tickets", html, pngs);
        }

        var sch = booking.getSchedule();
        var showText = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm").format(sch.getSessionStartTime());

        return new BookingConfirmationVM(
                booking.getId(),
                movieTitle(sch.getMovie().getId()),
                screenName(sch.getScreen().getId()),
                showText,
                booking.getTotalPrice(),
                ticketsVM
        );
    }

    private String movieTitle(Long movieId) {
        return movieRepo.findById(movieId).map(Movie::getTitle).orElse("-");
    }

    private String screenName(Long screenId) {
        return "Screen " + screenId;
    }
}