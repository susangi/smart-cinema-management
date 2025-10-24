package com.example.cinema_management.booking.repository;

import com.example.cinema_management.booking.service.BookingService;
import com.example.cinema_management.schedule.entity.Schedule;

import java.util.Optional;

public interface ShowTimeGateway {
    Optional<Schedule> getSchedule(Long scheduleId);
}
