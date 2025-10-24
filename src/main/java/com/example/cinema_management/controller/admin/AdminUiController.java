package com.example.cinema_management.controller.admin;

import com.example.cinema_management.user.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminUiController {

    private final UserRepository userRepository;

    public AdminUiController(UserRepository userRepository) {
        this.userRepository = userRepository;
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

        model.addAttribute("activeMovies", 4);
        model.addAttribute("totalUsers", 1);
        model.addAttribute("schedules", 3);
        model.addAttribute("totalRevenue", 139.92);

        return "admin/dashboard";
    }
}