package com.example.cinema_management.movie.web;

import com.example.cinema_management.movie.service.MovieService;
import com.example.cinema_management.schedule.repository.ScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/movies")
public class PublicMovieController {

    private final MovieService movieService;
    private final ScheduleRepository scheduleRepo;

    public PublicMovieController(MovieService movieService, ScheduleRepository scheduleRepo) {
        this.movieService = movieService;
        this.scheduleRepo = scheduleRepo;
    }

    // GET /movies
    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "12") int size,
                       Model model) {
        var results = movieService.searchPublic(q, page, size);
        model.addAttribute("page", results);
        model.addAttribute("q", q);
        return "public/movies/list";
    }

    // GET /movies/{id}
    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        var movie = movieService.get(id);
        if (movie == null) throw new EntityNotFoundException("Movie not found");
        model.addAttribute("movie", movie);
        return "public/movies/details";
    }

    // GET /movies/{id}/showtimes
    @GetMapping("/{id}/showtimes")
    public String showtimes(@PathVariable Long id, Model model) {
        var movie = movieService.get(id);
        if (movie == null) throw new EntityNotFoundException("Movie not found");
        var upcoming = scheduleRepo.findUpcomingByMovie(id, LocalDateTime.now());
        model.addAttribute("movie", movie);
        model.addAttribute("showtimes", upcoming);
        return "public/movies/showtimes";
    }
}
