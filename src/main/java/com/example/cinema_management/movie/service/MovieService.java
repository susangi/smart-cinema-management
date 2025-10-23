package com.example.cinema_management.movie.service;

import com.example.cinema_management.movie.dto.MovieCreateRequest;
import com.example.cinema_management.movie.dto.MovieUpdateRequest;
import com.example.cinema_management.movie.dto.MovieResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MovieService {

    Page<MovieResponse> list(String q, Pageable pageable);

    // Shims used by controllers
    Page<MovieResponse> searchAdmin(String q, int page, int size);
    Page<MovieResponse> searchPublic(String q, int page, int size);

    MovieResponse get(Long id);

    MovieResponse create(MovieCreateRequest req, MultipartFile poster);

    // NOTE: update uses MovieUpdateRequest to match your controller
    MovieResponse update(Long id, MovieUpdateRequest req, MultipartFile poster);

    void delete(Long id);

    MovieResponse toggleActive(Long id, boolean active);


    List<MovieResponse> findAllActive();
}
