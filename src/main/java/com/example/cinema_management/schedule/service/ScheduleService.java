package com.example.cinema_management.schedule.service;

import com.example.cinema_management.schedule.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ScheduleService {
    Page<ScheduleResponse> list(String q, Pageable pageable);
    ScheduleResponse get(Long id);
    ScheduleResponse create(ScheduleCreateRequest req);
    ScheduleResponse update(ScheduleUpdateRequest req);
    void delete(Long id);
}
