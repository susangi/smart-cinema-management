package com.example.cinema_management.screen.service;

import com.example.cinema_management.screen.dto.*;
import org.springframework.data.domain.Page;

public interface ScreenService {
    Page<ScreenResponse> search(String q, int page, int size);

    ScreenResponse get(Long id);

    ScreenResponse create(ScreenCreateRequest req);

    ScreenResponse update(Long id, ScreenUpdateRequest req);

    void delete(Long id);
}


