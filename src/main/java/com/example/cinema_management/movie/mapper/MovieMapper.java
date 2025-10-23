package com.example.cinema_management.movie.mapper;

import com.example.cinema_management.movie.dto.MovieCreateRequest;
import com.example.cinema_management.movie.dto.MovieResponse;
import com.example.cinema_management.movie.entity.Movie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MovieMapper {

    // Base URL that serves posters from disk (see WebMvcConfig)
    private final String postersUrlBase;

    public MovieMapper(@Value("${app.media.posters-url-base:/media/posters}") String postersUrlBase) {
        this.postersUrlBase = postersUrlBase.endsWith("/") ? postersUrlBase.substring(0, postersUrlBase.length() - 1) : postersUrlBase;

    }

    public Movie toEntity(MovieCreateRequest r) {
        if (r == null) return null;

        Movie m = new Movie();
        m.setTitle(r.getTitle());
        m.setDescription(r.getDescription());
        m.setDurationMinutes(r.getDurationMinutes());
        m.setLanguage(r.getLanguage());
        m.setGenre(r.getGenre());
        m.setRating(r.getRating());

        m.setReleaseDate(r.getReleaseDate());
        m.setActive(Boolean.TRUE.equals(r.getActive()));
        return m;
    }


    public MovieResponse toResponse(Movie m) {
        if (m == null) return null;
        String posterUrl = (m.getPosterPath() == null || m.getPosterPath().isBlank())
                ? null
                : postersUrlBase + "/" + m.getPosterPath();

        return new MovieResponse(
                m.getId(),
                m.getTitle(),
                m.getDescription(),
                m.getDurationMinutes(),
                m.getLanguage(),
                m.getGenre(),
                m.getRating(),
                m.getReleaseDate(),
                m.getPosterPath(),  // raw stored filename
                posterUrl,          // public URL
                m.isActive()
        );
    }



}

