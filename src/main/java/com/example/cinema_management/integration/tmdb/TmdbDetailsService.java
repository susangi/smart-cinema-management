package com.example.cinema_management.integration.tmdb;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Service
public class TmdbDetailsService {

    private final String apiKey;
    private final RestTemplate http = new RestTemplate();

    public TmdbDetailsService(@Value("${tmdb.api-key}") String apiKey) {
        this.apiKey = apiKey;
    }

    public MovieDetails get(long id, String language) {
        String lang = (language == null || language.isBlank()) ? "en-US" : language;
        URI uri = UriComponentsBuilder.fromUriString("https://api.themoviedb.org/3/movie/{id}")
                .queryParam("api_key", apiKey)
                .queryParam("language", lang)
                .buildAndExpand(id)
                .encode()
                .toUri();
        try {
            ResponseEntity<MovieDetails> res = http.getForEntity(uri, MovieDetails.class);
            MovieDetails body = res.getBody();
            if (body == null) throw new IllegalStateException("Empty TMDB response");
            return body;
        } catch (RestClientException e) {
            throw new IllegalStateException("TMDB details fetch failed: " + e.getMessage(), e);
        }
    }

    // ===== minimal DTOs we need =====
    public static record Genre(int id, String name) {}
    public static record MovieDetails(
            long id,
            String title,
            String overview,
            Integer runtime,
            String original_language,
            String release_date,
            String poster_path,
            Double vote_average,
            List<Genre> genres
    ) {}
}
