package com.example.cinema_management.screen.dto;

import java.io.Serializable;

public record ScreenResponse(
        Long id,
        String name,
        String code,
        String description,
        String type,
        boolean active,
        Integer capacity
) implements Serializable {}
