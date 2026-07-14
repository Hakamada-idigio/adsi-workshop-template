package com.example.attendance.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "社員番号は必須です")
        String employeeNumber,

        @NotBlank(message = "パスワードは必須です")
        String password
) {}
