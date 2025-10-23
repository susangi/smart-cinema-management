package com.example.cinema_management.schedule.mapper;

import com.example.cinema_management.schedule.dto.ScheduleResponse;
import com.example.cinema_management.schedule.entity.Schedule;
import org.springframework.stereotype.Component;

@Component
public class ScheduleMapper {
    public ScheduleResponse toResponse(Schedule s) {
        return new ScheduleResponse(
                s.getId(),
                s.getMovie().getId(), s.getMovie().getTitle(),
                s.getPricing().getId(), s.getPricing().getName(), // adjust if your Pricing label differs
                s.getScreen().getId(), s.getScreen().getName(), s.getScreen().getCode(),
                s.getSessionStartTime(),
                s.getSessionEndTime(),
                s.getStatus().name()
        );
    }
}
