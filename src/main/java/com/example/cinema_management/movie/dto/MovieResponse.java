package com.example.cinema_management.movie.dto;

import java.time.LocalDate;

public record MovieResponse(
        Long id,
        String title,
        String description,
        Integer durationMinutes,
        String language,
        String genre,
        Double rating,
        LocalDate releaseDate,
        String posterPath,
        String posterUrl,
        boolean active
) {}
