package com.example.cinema_management.schedule.repository;

import com.example.cinema_management.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository
        extends JpaRepository<Schedule, Long>, JpaSpecificationExecutor<Schedule> {

    /**
     * Find ACTIVE schedules that overlap a given (start,end) on the same screen.
     * If editing, pass the current id in ignoreId to exclude it.
     *
     * Overlap condition:
     *    existing.start < newEnd  AND  existing.end > newStart
     * (strict inequalities => touching at a point is allowed; buffer is applied in service)
     */
    @Query("""
        select s from Schedule s
        where s.screen.id = :screenId
          and s.status = com.example.cinema_management.schedule.entity.Schedule$Status.ACTIVE
          and (:ignoreId is null or s.id <> :ignoreId)
          and (s.sessionStartTime < :endTime and s.sessionEndTime > :startTime)
        """)
    List<Schedule> findConflicts(@Param("screenId") Long screenId,
                                 @Param("startTime") LocalDateTime startTime,
                                 @Param("endTime") LocalDateTime endTime,
                                 @Param("ignoreId") Long ignoreId);


    @Query("""
        select s from Schedule s
         join fetch s.screen sc
         join fetch s.pricing p
        where s.movie.id = :movieId
          and s.status = com.example.cinema_management.schedule.entity.Schedule$Status.ACTIVE
          and s.sessionStartTime >= :now
        order by s.sessionStartTime asc
    """)
    List<Schedule> findUpcomingByMovie(@Param("movieId") Long movieId,
                                       @Param("now") LocalDateTime now);



    List<Schedule> findByMovieId(Long movieId);

    Optional<Schedule> findById(Long id);

    List<Schedule> findBySessionStartTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Schedule> findBySessionEndTimeBetween(LocalDateTime start, LocalDateTime end);

    // Optional custom range query
    @Query("""
           select s
           from Schedule s
           where s.sessionStartTime >= :start
             and s.sessionEndTime   <= :end
           """)
    List<Schedule> findActiveSessionsBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // @Query("SELECT COUNT(s) FROM Schedule s WHERE s.sessionStartTime > CURRENT_TIMESTAMP AND s.status = 'ACTIVE'")
    // long countUpcomingSchedules();
}
