package com.example.cinema_management.auth.dto;

import com.example.cinema_management.user.Role;
import jakarta.validation.constraints.*;

public class RegisterRequest {
    @NotBlank @Size(min = 2, max = 100)
    private String name;

    @NotBlank @Email @Size(max = 100)
    private String email;

    @NotBlank @Size(min = 6, max = 72)
    private String password;

    @NotNull
    private Role role;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
