package com.example.cinema_management.screen.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScreenUpdateRequest {
    @NotBlank
    @Size(max = 120)
    private String name;
    @NotBlank
    @Size(max = 30)
    private String code;
    @Size(max = 500)
    private String description;
    @Size(max = 10)
    private String type;
    @Min(0)
    private Integer capacity;
    private boolean active;
}
