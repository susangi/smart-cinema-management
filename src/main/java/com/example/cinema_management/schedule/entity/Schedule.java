package com.example.cinema_management.schedule.entity;

import com.example.cinema_management.movie.entity.Movie;
import com.example.cinema_management.pricing.Pricing;
import com.example.cinema_management.screen.entity.Screen;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "schedules",
        indexes = @Index(name = "ix_sched_screen_time", columnList = "screen_id, session_start_time"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Schedule {

    public enum Status { ACTIVE, CANCELLED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "rate_card_id")
    private Pricing pricing;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id")
    private Screen screen;

    @Column(name = "session_start_time", nullable = false)
    private LocalDateTime sessionStartTime;

    @Column(name = "session_end_time", nullable = false)
    private LocalDateTime sessionEndTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    public void onCreate() {
        final var now = LocalDateTime.now();
        createdAt = now; updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
