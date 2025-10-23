package com.example.cinema_management.schedule.dto;

import java.time.LocalDateTime;

public record ScheduleResponse(
        Long id,
        Long movieId, String movieTitle,
        Long rateCardId, String rateCardName,
        Long screenId, String screenName, String screenCode,
        LocalDateTime sessionStartTime,
        LocalDateTime sessionEndTime,
        String status
) {}
