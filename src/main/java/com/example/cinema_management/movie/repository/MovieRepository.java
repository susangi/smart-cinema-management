package com.example.cinema_management.movie.repository;

import com.example.cinema_management.movie.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    Optional<Movie> findFirstByTitleIgnoreCase(String title);
    Page<Movie> findByTitleContainingIgnoreCaseAndActiveTrue(String title, Pageable pageable);
    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    @Query("select m from Movie m where m.active = true order by m.title asc")
    List<Movie> findAllActive();

//    @Query("SELECT COUNT(m) FROM Movie m WHERE m.active = 1")
//    long countActiveMovies();

}
