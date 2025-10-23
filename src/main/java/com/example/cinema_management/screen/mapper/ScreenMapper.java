package com.example.cinema_management.screen.mapper;

import com.example.cinema_management.screen.dto.*;
import com.example.cinema_management.screen.entity.Screen;
import org.springframework.stereotype.Component;

@Component
public class ScreenMapper {

    public Screen toEntity(ScreenCreateRequest r) {
        return Screen.builder()
                .name(r.getName())
                .code(r.getCode())
                .description(r.getDescription())
                .type(r.getType())
                .capacity(r.getCapacity())
                .active(r.isActive())
                .build();
    }


    public void update(Screen e, ScreenUpdateRequest r) {
        e.setName(r.getName());
        e.setCode(r.getCode());
        e.setDescription(r.getDescription());
        e.setType(r.getType());
        e.setCapacity(r.getCapacity());
        e.setActive(r.isActive());
    }

    public ScreenResponse toResponse(Screen s) {
        return new ScreenResponse(
                s.getId(), s.getName(), s.getCode(), s.getDescription(), s.getType(),
                 s.isActive(), s.getCapacity()
        );
    }
}
