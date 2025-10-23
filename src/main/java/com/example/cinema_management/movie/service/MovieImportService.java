package com.example.cinema_management.movie.service;


import com.example.cinema_management.movie.dto.MovieResponse;


public interface MovieImportService {
    MovieResponse importFromTmdb(long tmdbId, String lang, String region);

}