package com.example.cinema_management.movie.service;

import org.springframework.web.multipart.MultipartFile;

public interface PosterStorageService {
    String storePoster(MultipartFile file, Long movieId);
    void deletePosterIfExists(String path);
    String storePosterFromUrl(String url, Long movieId);
}
