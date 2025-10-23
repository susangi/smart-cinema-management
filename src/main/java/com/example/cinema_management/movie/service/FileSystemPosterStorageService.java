package com.example.cinema_management.movie.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.time.Instant;
import java.util.UUID;

@Service
public class FileSystemPosterStorageService implements PosterStorageService {

    private final Path root;

    public FileSystemPosterStorageService(@Value("${app.media.posters-dir:uploads/posters}") String dir) {
        this.root = Paths.get(dir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new RuntimeException("Could not create posters directory", e);
        }
    }

    @Override
    public String storePoster(MultipartFile file, Long movieId) {
        if (file == null || file.isEmpty()) return null;
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String filename = buildFilename(movieId, ext);
        Path target = root.resolve(filename);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store poster", e);
        }
    }

    @Override
    public String storePosterFromUrl(String url, Long movieId) {
        if (url == null || url.isBlank()) return null;
        try {
            String ext = guessExtension(url);
            String filename = buildFilename(movieId, ext);
            Path target = root.resolve(filename);

            HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<InputStream> res = client.send(req, HttpResponse.BodyHandlers.ofInputStream());
            if (res.statusCode() >= 200 && res.statusCode() < 300) {
                Files.copy(res.body(), target, StandardCopyOption.REPLACE_EXISTING);
                return filename;
            } else {
                throw new RuntimeException("Failed to download poster (HTTP " + res.statusCode() + ")");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to download poster from URL: " + url, e);
        }
    }

    @Override
    public void deletePosterIfExists(String path) {
        if (path == null || path.isBlank()) return;
        try { Files.deleteIfExists(root.resolve(path)); } catch (IOException ignored) {}
    }

    private String buildFilename(Long movieId, String ext) {
        String safeExt = (ext != null && !ext.isBlank()) ? ext.toLowerCase() : "jpg";
        return "m" + movieId + "_" + UUID.randomUUID() + "_" + Instant.now().toEpochMilli() + "." + safeExt;
    }

    private String guessExtension(String url) {
        try {
            String name = Paths.get(URI.create(url).getPath()).getFileName().toString();
            int dot = name.lastIndexOf('.');
            if (dot > 0 && dot < name.length() - 1) {
                String cand = name.substring(dot + 1).toLowerCase();
                if (cand.matches("[a-z0-9]{2,5}")) return cand;
            }
        } catch (Exception ignored) {}
        return "jpg";
    }
}
