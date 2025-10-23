package com.example.cinema_management.movie.service;

import com.example.cinema_management.integration.tmdb.TmdbDetailsService;
import com.example.cinema_management.movie.dto.MovieCreateRequest;
import com.example.cinema_management.movie.dto.MovieResponse;
import com.example.cinema_management.movie.entity.Movie;
import com.example.cinema_management.movie.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
public class MovieImportServiceImpl implements MovieImportService {

    private final TmdbDetailsService tmdb;
    private final MovieRepository movieRepo;
    private final MovieService movieService;
    private final PosterStorageService posterStorage;

    private final boolean downloadPosters;
    private final String tmdbImageBaseUrl;
    private final String tmdbImageSize;

    public MovieImportServiceImpl(
            TmdbDetailsService tmdb,
            MovieRepository movieRepo,
            MovieService movieService,
            PosterStorageService posterStorage,
            @Value("${tmdb.download-posters:true}") boolean downloadPosters,
            @Value("${tmdb.image-base-url:https://image.tmdb.org/t/p/}") String tmdbImageBaseUrl,
            @Value("${tmdb.image-size:w500}") String tmdbImageSize
    ) {
        this.tmdb = tmdb;
        this.movieRepo = movieRepo;
        this.movieService = movieService;
        this.posterStorage = posterStorage;
        this.downloadPosters = downloadPosters;
        this.tmdbImageBaseUrl = tmdbImageBaseUrl;
        this.tmdbImageSize = tmdbImageSize;
    }

    @Override
    @Transactional
    public MovieResponse importFromTmdb(long tmdbId, String lang, String region) {
        var d = tmdb.get(tmdbId, lang);
        if (d.title() == null || d.title().isBlank()) {
            throw new IllegalStateException("TMDB movie not found for id " + tmdbId);
        }

        // dedupe by title
        var existing = movieRepo.findFirstByTitleIgnoreCase(d.title());
        if (existing.isPresent()) {
            return movieService.get(existing.get().getId());
        }

        Integer runtime = (d.runtime() != null && d.runtime() > 0) ? d.runtime() : null;
        String genreJoined = (d.genres() == null || d.genres().isEmpty())
                ? null
                : d.genres().stream().map(TmdbDetailsService.Genre::name).collect(Collectors.joining(", "));

        LocalDate release = null;
        if (d.release_date() != null && !d.release_date().isBlank()) {
            try { release = LocalDate.parse(d.release_date()); } catch (Exception ignored) {}
        }

        var req = MovieCreateRequest.builder()
                .title(d.title())
                .description(d.overview())
                .durationMinutes(runtime)
                .language(d.original_language())
                .genre(genreJoined)
                .rating(d.vote_average())
                .releaseDate(release)
                .active(true)
                .build();

        // Save first (needed to name the poster)
        var created = movieService.create(req, null);

        // Download poster (optional)
        if (downloadPosters && d.poster_path() != null && !d.poster_path().isBlank()) {
            String normalized = d.poster_path().startsWith("/") ? d.poster_path().substring(1) : d.poster_path();
            String remotePosterUrl = tmdbImageBaseUrl + tmdbImageSize + "/" + normalized;
            String saved = posterStorage.storePosterFromUrl(remotePosterUrl, created.id());
            if (saved != null) {
                // persist poster path on entity
                Movie m = movieRepo.findById(created.id()).orElseThrow();
                m.setPosterPath(saved);
                movieRepo.save(m);
            }
        }

        return movieService.get(created.id());
    }
}
