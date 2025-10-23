package com.example.cinema_management.screen.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.cinema_management.screen.dto.*;
import com.example.cinema_management.screen.entity.Screen;
import com.example.cinema_management.screen.mapper.ScreenMapper;
import com.example.cinema_management.screen.repository.ScreenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScreenServiceImpl implements ScreenService {

    private final ScreenRepository repo;
    private final ScreenMapper mapper;
    private final ObjectMapper om = new ObjectMapper();

    @Override
    @Transactional(readOnly = true)
    public Page<ScreenResponse> search(String q, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page,0), Math.max(size,1), Sort.by("name").ascending());
        String t = (q == null) ? "" : q;
        return repo.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(t, t, pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ScreenResponse get(Long id) {
        return repo.findById(id).map(mapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Screen not found: " + id));
    }

    @Override
    @Transactional
    public ScreenResponse create(ScreenCreateRequest req) {
        Screen s = mapper.toEntity(req);
        // capacity initially null / 0 until seat map saved
        s = repo.save(s);
        return mapper.toResponse(s);
    }

    @Override
    @Transactional
    public ScreenResponse update(Long id, ScreenUpdateRequest req) {
        Screen s = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Screen not found: " + id));
        mapper.update(s, req);
        return mapper.toResponse(repo.save(s));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }


}
