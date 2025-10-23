package com.example.cinema_management.movie.web;

import com.example.cinema_management.integration.tmdb.TmdbSearchService;
import com.example.cinema_management.movie.dto.MovieCreateRequest;
import com.example.cinema_management.movie.dto.MovieUpdateRequest;
import com.example.cinema_management.movie.service.MovieImportService;
import com.example.cinema_management.movie.service.MovieService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// TMDB
import com.example.cinema_management.integration.tmdb.TmdbClient;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/movies")
public class AdminMovieController {

    private final MovieService svc;
    private final MovieImportService movieImportService;
    private final TmdbClient tmdb;
    private final TmdbSearchService tmdbSearch;

    @Value("${tmdb.image-base-url:https://image.tmdb.org/t/p/}")
    private String tmdbImageBaseUrl;

    @Value("${tmdb.image-size:w500}")
    private String tmdbImageSize;

    public AdminMovieController(MovieService svc,
                                MovieImportService movieImportService,
                                TmdbClient tmdb,
                                TmdbSearchService tmdbSearch) {
        this.svc = svc;
        this.movieImportService = movieImportService;
        this.tmdb = tmdb;
        this.tmdbSearch = tmdbSearch;
    }

    // ----------- LIST -----------
    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        var pageable = PageRequest.of(Math.max(page,0), Math.max(size,1), Sort.by("title").ascending());
        var results = svc.searchAdmin(q, page, size); // shim calls list(...)
        model.addAttribute("page", results);
        model.addAttribute("q", q);
        return "admin/movies/list";
    }

    // ----------- CREATE -----------
    @GetMapping("/new")
    public String formCreate(Model model) {
        model.addAttribute("movie", MovieCreateRequest.builder()
                .title("").description("").durationMinutes(0)
                .language("").genre("").rating(0.0)
                .releaseDate(null).active(true)
                .build());
        return "admin/movies/form";
    }

    @PostMapping
    public String create(@ModelAttribute("movie") @Valid MovieCreateRequest req,
                         @RequestParam(value = "poster", required = false) MultipartFile poster,
                         RedirectAttributes ra) {
        svc.create(req, poster);
        ra.addFlashAttribute("saved", true);
        return "redirect:/admin/movies";
    }

    // ----------- EDIT/UPDATE -----------
    @GetMapping("/{id}/edit")
    public String formEdit(@PathVariable Long id, Model model) {
        var resp = svc.get(id);
        model.addAttribute("movieResp", resp);
        model.addAttribute("movie", MovieUpdateRequest.builder()
                .title(resp.title())
                .description(resp.description())
                .durationMinutes(resp.durationMinutes())
                .language(resp.language())
                .genre(resp.genre())
                .rating(resp.rating())
                .releaseDate(resp.releaseDate())
                .active(resp.active())
                .build());
        return "admin/movies/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute("movie") @Valid MovieUpdateRequest req,
                         @RequestParam(value = "poster", required = false) MultipartFile poster,
                         RedirectAttributes ra) {
        svc.update(id, req, poster);
        ra.addFlashAttribute("saved", true);
        return "redirect:/admin/movies";
    }

    // ----------- DELETE -----------
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        svc.delete(id);
        return "redirect:/admin/movies";
    }



    // ========== Helpers ==========

    private List<TmdbItem> searchTmdbSafe(String query, int page) {
        try {
            // Try common signatures: searchMovies(String,int) or search(String,int)
            Method m1 = findMethod(tmdb.getClass(),
                    new String[]{"searchMovies", "search"},
                    new Class[]{String.class, int.class});
            if (m1 != null) {
                Object res = m1.invoke(tmdb, query, page);
                return extractResults(res);
            }
            // Fallback: searchMovies(String)
            Method m2 = findMethod(tmdb.getClass(),
                    new String[]{"searchMovies", "search"},
                    new Class[]{String.class});
            if (m2 != null) {
                Object res = m2.invoke(tmdb, query);
                return extractResults(res);
            }
        } catch (Exception ignored) {}
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private List<TmdbItem> extractResults(Object apiResult) {
        // apiResult might already be List<TmdbSearchResult>; otherwise try to get a "results" field via getter/record method
        List<?> raw;
        if (apiResult instanceof List<?>) {
            raw = (List<?>) apiResult;
        } else {
            try {
                var method = apiResult.getClass().getMethod("results");
                Object r = method.invoke(apiResult);
                raw = (List<?>) r;
            } catch (Exception e) {
                return List.of();
            }
        }

        List<TmdbItem> out = new ArrayList<>();
        for (Object o : raw) {
            try {
                long id = (long) invokeAny(o, new String[]{"id"});
                String title = (String) invokeAny(o, new String[]{"title", "name"});
                String release = (String) invokeAny(o, new String[]{"release_date", "first_air_date"}, true);
                String overview = (String) invokeAny(o, new String[]{"overview"}, true);
                String lang = (String) invokeAny(o, new String[]{"original_language"}, true);
                String posterPath = (String) invokeAny(o, new String[]{"poster_path"}, true);

                String posterUrl = (posterPath == null || posterPath.isBlank())
                        ? null
                        : tmdbImageBaseUrl + tmdbImageSize + "/" + (posterPath.startsWith("/") ? posterPath.substring(1) : posterPath);

                out.add(new TmdbItem(id, title, release, overview, posterUrl, lang));
            } catch (Exception ignored) {}
        }
        return out;
    }

    private Object invokeAny(Object target, String[] names) throws Exception { return invokeAny(target, names, false); }
    private Object invokeAny(Object target, String[] names, boolean nullable) throws Exception {
        for (String n : names) {
            try {
                Method m = target.getClass().getMethod(n);
                return m.invoke(target);
            } catch (NoSuchMethodException ignored) {}
        }
        if (nullable) return null;
        throw new NoSuchMethodException("no accessor");
    }

    private Method findMethod(Class<?> cls, String[] names, Class<?>[] params) {
        for (String name : names) {
            try { return cls.getMethod(name, params); } catch (NoSuchMethodException ignored) {}
        }
        return null;
    }

    // View model for TMDB search
    public record TmdbItem(long id, String title, String releaseDate,
                           String overview, String posterUrl, String language) {}





    // replace your /tmdb GET handler with this one:
    @GetMapping("/tmdb")
    public String tmdbSearch(@RequestParam(required = false) String q,
                             @RequestParam(defaultValue = "1") int page,
                             @RequestParam(required = false, defaultValue = "en-US") String lang,
                             Model model) {

        var search = (q == null || q.isBlank())
                ? TmdbSearchService.SearchResult.empty()
                : tmdbSearch.searchMovies(q, page, lang);

        model.addAttribute("q", q);
        model.addAttribute("lang", lang);
        model.addAttribute("pageNum", search.page());
        model.addAttribute("totalPages", search.totalPages());
        model.addAttribute("results", search.results());
        return "admin/movies/tmdb";
    }


    @PostMapping("/tmdb/import")
    public String importFromTmdb(@RequestParam("tmdbId") Long tmdbId,
                                 RedirectAttributes ra) {
        try {
            var movie = movieImportService.importFromTmdb(tmdbId, null, null);
            ra.addFlashAttribute("saved", true);
            ra.addFlashAttribute("importedTitle", movie.title());
            return "redirect:/admin/movies";
        } catch (Exception ex) {
            ra.addFlashAttribute("error", "TMDB import failed: " + ex.getMessage());
            return "redirect:/admin/movies/tmdb";   // show the error on the TMDB page
        }
    }


}
