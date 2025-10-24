package com.example.cinema_management.controller;

import com.example.cinema_management.movie.repository.MovieRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

//    @GetMapping("/")
//    public String homePage() {
//        return "index"; // loads index.html from templates/
//    }

    private final MovieRepository movieRepo;

    public HomeController(MovieRepository movieRepo) {
        this.movieRepo = movieRepo;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("movies", movieRepo.findAllActive());
        return "public/home";
    }
}
