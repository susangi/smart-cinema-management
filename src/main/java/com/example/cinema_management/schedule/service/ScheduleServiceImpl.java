package com.example.cinema_management.schedule.service;

import com.example.cinema_management.movie.repository.MovieRepository;
import com.example.cinema_management.pricing.PricingRepository;
import com.example.cinema_management.schedule.dto.ScheduleCreateRequest;
import com.example.cinema_management.schedule.dto.ScheduleResponse;
import com.example.cinema_management.schedule.dto.ScheduleUpdateRequest;
import com.example.cinema_management.schedule.entity.Schedule;
import com.example.cinema_management.schedule.mapper.ScheduleMapper;
import com.example.cinema_management.schedule.repository.ScheduleRepository;
import com.example.cinema_management.screen.repository.ScreenRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Join;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository repo;
    private final MovieRepository movieRepo;
    private final PricingRepository pricingRepo;
    private final ScreenRepository screenRepo;
    private final ScheduleMapper mapper;

    @Value("${schedules.bufferMinutes:0}")
    private int bufferMinutes;

    @Override
    @Transactional(readOnly = true)
    public Page<ScheduleResponse> list(String q, Pageable pageable) {
        Specification<Schedule> spec = (root, cq, cb) -> {
            if (q == null || q.isBlank()) {
                return cb.conjunction();
            }
            String like = "%" + q.trim().toLowerCase() + "%";

            Join<?, ?> m  = root.join("movie");
            Join<?, ?> sc = root.join("screen");
            Join<?, ?> p  = root.join("pricing");

            return cb.or(
                    cb.like(cb.lower(m.get("title")), like),
                    cb.like(cb.lower(sc.get("name")), like),
                    cb.like(cb.lower(sc.get("code")), like),
                    cb.like(cb.lower(p.get("name")), like)
            );
        };

        return repo.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ScheduleResponse get(Long id) {
        var s = repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Schedule not found"));
        return mapper.toResponse(s);
    }

    @Override
    public ScheduleResponse create(ScheduleCreateRequest req) {
        var movie  = movieRepo.findById(req.movieId()).orElseThrow();
        var pricing = pricingRepo.findById(req.rateCardId()).orElseThrow();
        var screen = screenRepo.findById(req.screenId()).orElseThrow();

        int minutes = movie.getDurationMinutes(); // adjust getter name if different
        if (minutes <= 0) throw new IllegalArgumentException("Movie duration must be > 0 minutes.");

        LocalDateTime start = req.sessionStartTime();
        LocalDateTime end   = start.plusMinutes(minutes);

        validateNoOverlap(screen.getId(), start, end, null);

        var s = Schedule.builder()
                .movie(movie)
                .pricing(pricing)
                .screen(screen)
                .sessionStartTime(start)
                .sessionEndTime(end)
                .status(Schedule.Status.ACTIVE)
                .build();

        return mapper.toResponse(repo.save(s));
    }

    @Override
    public ScheduleResponse update(ScheduleUpdateRequest req) {
        var s = repo.findById(req.id()).orElseThrow(() -> new EntityNotFoundException("Schedule not found"));

        var movie  = movieRepo.findById(req.movieId()).orElseThrow();
        var pricing = pricingRepo.findById(req.rateCardId()).orElseThrow();
        var screen = screenRepo.findById(req.screenId()).orElseThrow();

        int minutes = movie.getDurationMinutes(); // adjust getter name if different
        if (minutes <= 0) throw new IllegalArgumentException("Movie duration must be > 0 minutes.");

        LocalDateTime start = req.sessionStartTime();
        LocalDateTime end   = start.plusMinutes(minutes);

        validateNoOverlap(screen.getId(), start, end, s.getId());

        s.setMovie(movie);
        s.setPricing(pricing);
        s.setScreen(screen);
        s.setSessionStartTime(start);
        s.setSessionEndTime(end);
        if (req.status() != null) s.setStatus(Schedule.Status.valueOf(req.status()));

        return mapper.toResponse(s);
    }

    /**
     * Checks for conflicts on the same screen.
     * Applies a configurable buffer on BOTH sides: [start - buffer, end + buffer].
     * If conflicts exist, throws an exception with details.
     */
    private void validateNoOverlap(Long screenId, LocalDateTime start, LocalDateTime end, Long ignoreId) {
        int buf = Math.max(0, bufferMinutes);
        var paddedStart = start.minusMinutes(buf);
        var paddedEnd   = end.plusMinutes(buf);

        var conflicts = repo.findConflicts(screenId, paddedStart, paddedEnd, ignoreId);
        if (!conflicts.isEmpty()) {
            var c = conflicts.get(0);
            String msg = "Conflicts with existing show: "
                    + c.getMovie().getTitle()
                    + " on " + c.getScreen().getName()
                    + " (" + c.getScreen().getCode() + ") "
                    + "from " + c.getSessionStartTime()
                    + " to " + c.getSessionEndTime()
                    + (buf > 0 ? " (including " + buf + " min buffer)" : "");
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
