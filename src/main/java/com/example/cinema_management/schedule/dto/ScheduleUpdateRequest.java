package com.example.cinema_management.schedule.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record ScheduleUpdateRequest(
        @NotNull Long id,
        @NotNull Long movieId,
        @NotNull Long rateCardId,
        @NotNull Long screenId,
        @NotNull LocalDateTime sessionStartTime,
        String status // "ACTIVE" or "CANCELLED"
) {}
