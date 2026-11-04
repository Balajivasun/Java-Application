package com.skyline.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Username/Student ID or Email is required")
    private String username; // Can be email or studentId

    @NotBlank(message = "Password is required")
    private String password;
}
