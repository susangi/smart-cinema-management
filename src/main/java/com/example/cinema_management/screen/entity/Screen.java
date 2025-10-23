package com.example.cinema_management.screen.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "screens", indexes = {
        @Index(name = "ix_screens_name", columnList = "name"),
        @Index(name = "ux_screens_code", columnList = "code", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Screen {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String name;

    /** Human/ops code like SCR-01, unique */
    @NotBlank @Size(max = 30)
    @Column(nullable = false, length = 30, unique = true)
    private String code;

    @Size(max = 500)
    @Column(length = 500)
    private String description;

    /** 2D / 3D / IMAX / 4DX */
    @Size(max = 10)
    @Column(length = 10)
    private String type;


    /** If you hide screens from scheduling */
    @Column(nullable = false)
    private boolean active = true;


    private Integer capacity;


}
