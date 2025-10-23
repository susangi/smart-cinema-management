package com.example.cinema_management.movie.service;

import com.example.cinema_management.movie.dto.MovieCreateRequest;
import com.example.cinema_management.movie.dto.MovieUpdateRequest;
import com.example.cinema_management.movie.dto.MovieResponse;
import com.example.cinema_management.movie.entity.Movie;
import com.example.cinema_management.movie.mapper.MovieMapper;
import com.example.cinema_management.movie.repository.MovieRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class MovieServiceImpl implements MovieService {

    private final MovieRepository repo;
    private final MovieMapper mapper;
    private final PosterStorageService posterStorage;

    public MovieServiceImpl(MovieRepository repo, MovieMapper mapper, PosterStorageService posterStorage) {
        this.repo = repo;
        this.mapper = mapper;
        this.posterStorage = posterStorage;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieResponse> list(String q, Pageable pageable) {
        String query = (q == null) ? "" : q;
        return repo.findByTitleContainingIgnoreCaseAndActiveTrue(query, pageable)
                .map(mapper::toResponse);
    }


    @Override
    public Page<MovieResponse> searchAdmin(String q, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("title").ascending());
        var query = (q == null) ? "" : q;
        var results = repo.findByTitleContainingIgnoreCase(query, pageable);
        return results.map(mapper::toResponse);
    }



    @Override
    @Transactional(readOnly = true)
    public Page<MovieResponse> searchPublic(String q, int page, int size) {
        int p = Math.max(page, 0);
        int s = Math.max(size, 1);
        Pageable pageable = PageRequest.of(p, s,
                Sort.by("releaseDate").descending().and(Sort.by("title").ascending()));
        return list(q, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public MovieResponse get(Long id) {
        Movie m = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Movie not found: " + id));
        return mapper.toResponse(m);
    }

    @Override
    @Transactional
    public MovieResponse create(MovieCreateRequest req, MultipartFile poster) {
        Movie m = mapper.toEntity(req);
        m = repo.save(m);

        if (poster != null && !poster.isEmpty()) {
            String saved = posterStorage.storePoster(poster, m.getId());
            if (saved != null && !saved.isBlank()) {
                m.setPosterPath(saved);
                m = repo.save(m);
            }
        }
        return mapper.toResponse(m);
    }

    @Override
    @Transactional
    public MovieResponse update(Long id, MovieUpdateRequest req, MultipartFile poster) {
        Movie m = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Movie not found: " + id));

        m.setTitle(req.getTitle());
        m.setDescription(req.getDescription());
        m.setDurationMinutes(req.getDurationMinutes());
        m.setLanguage(req.getLanguage());
        m.setGenre(req.getGenre());
        m.setRating(req.getRating());
        m.setReleaseDate(req.getReleaseDate());
        m.setActive(Boolean.TRUE.equals(req.getActive()));

        if (poster != null && !poster.isEmpty()) {
            posterStorage.deletePosterIfExists(m.getPosterPath());
            String saved = posterStorage.storePoster(poster, m.getId());
            if (saved != null && !saved.isBlank()) {
                m.setPosterPath(saved);
            }
        }

        m = repo.save(m);
        return mapper.toResponse(m);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Movie m = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Movie not found: " + id));
        posterStorage.deletePosterIfExists(m.getPosterPath());
        repo.delete(m);
    }

    @Override
    @Transactional
    public MovieResponse toggleActive(Long id, boolean active) {
        Movie m = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Movie not found: " + id));
        m.setActive(active);
        m = repo.save(m);
        return mapper.toResponse(m);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieResponse> findAllActive() {
        return repo.findAllActive().stream()
                .map(mapper::toResponse)
                .toList();
    }
}
