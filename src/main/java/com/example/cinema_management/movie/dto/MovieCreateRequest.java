package com.example.cinema_management.movie.dto;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
public class MovieCreateRequest {
    private String title;
    private String description;
    private Integer durationMinutes;
    private String language;
    private String genre;

    @Column(precision = 4, scale = 2)
    private Double rating;           // numeric

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate releaseDate;
    private Boolean active;


    public MovieCreateRequest() {}

    public MovieCreateRequest(String title, String description, Integer durationMinutes,
                              String language, String genre, Double rating,
                              LocalDate releaseDate, Boolean active) {
        this.title = title;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.language = language;
        this.genre = genre;
        this.rating = rating;

        this.releaseDate = releaseDate;
        this.active = active;
    }
}
