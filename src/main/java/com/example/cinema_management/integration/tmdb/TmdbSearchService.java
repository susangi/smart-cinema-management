package com.example.cinema_management.integration.tmdb;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class TmdbSearchService {

    private final String apiKey;
    private final RestTemplate http = new RestTemplate();

    public TmdbSearchService(@Value("${tmdb.api-key}") String apiKey) {
        this.apiKey = apiKey;
    }

    public SearchResult searchMovies(String query, int page, String language) {
        if (query == null || query.isBlank()) return SearchResult.empty();
        String lang = (language == null || language.isBlank()) ? "en-US" : language;

        URI uri = UriComponentsBuilder.fromUriString("https://api.themoviedb.org/3/search/movie")
                .queryParam("api_key", apiKey)
                .queryParam("query", query)
                .queryParam("page", Math.max(page, 1))
                .queryParam("include_adult", false)
                .queryParam("language", lang)
                .build().encode(StandardCharsets.UTF_8).toUri();

        ResponseEntity<TmdbSearchResponse> res = http.getForEntity(uri, TmdbSearchResponse.class);
        TmdbSearchResponse body = res.getBody();
        if (body == null || body.results == null) return SearchResult.empty();

        return new SearchResult(body.page, body.total_pages, body.results);
    }

    // --- DTOs for the JSON we care about ---

    public static record TmdbSearchResponse(
            int page,
            int total_results,
            int total_pages,
            List<Item> results
    ) { }

    public static record Item(
            long id,
            String title,
            String overview,
            String release_date,
            String original_language,
            String poster_path
    ) { }

    public static record SearchResult(int page, int totalPages, List<Item> results) {
        public static SearchResult empty() { return new SearchResult(1, 1, List.of()); }
    }
}
