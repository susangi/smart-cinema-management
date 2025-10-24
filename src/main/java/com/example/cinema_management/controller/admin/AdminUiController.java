package com.example.cinema_management.controller.admin;

import com.example.cinema_management.booking.entity.BookingStatus;
import com.example.cinema_management.booking.repository.BookingRepository;
import com.example.cinema_management.movie.entity.Movie;
import com.example.cinema_management.movie.repository.MovieRepository;
import com.example.cinema_management.schedule.entity.Schedule;
import com.example.cinema_management.schedule.repository.ScheduleRepository;
import com.example.cinema_management.user.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;

@Controller
public class AdminUiController {

    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final ScheduleRepository scheduleRepository;
    private final BookingRepository bookingRepository;

    public AdminUiController(UserRepository userRepository, MovieRepository movieRepository, ScheduleRepository scheduleRepository, BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.scheduleRepository = scheduleRepository;
        this.bookingRepository = bookingRepository;
    }

    @GetMapping({"/admin", "/admin/dashboard"})
    public String dashboard(HttpSession session, Authentication authentication, Model model) {
        if (session.getAttribute("adminName") == null) {
            String email = authentication.getName();
            String adminName = userRepository.findByEmail(email)
                    .map(user -> user.getName())
                    .orElse("Admin User");
            session.setAttribute("adminName", adminName);
        }

        long activeMoviesCount = 3;
        long totalUsersCount = 2;
        long schedulesCount = 1;

        BigDecimal totalRevenue = null;

        model.addAttribute("activeMovies", activeMoviesCount);
        model.addAttribute("totalUsers", totalUsersCount);
        model.addAttribute("schedules", schedulesCount);
        model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

        return "admin/dashboard";
    }
}