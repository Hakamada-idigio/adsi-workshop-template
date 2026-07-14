package com.example.attendance.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "現在のパスワードは必須です")
        String currentPassword,

        @NotBlank(message = "新しいパスワードは必須です")
        @Size(min = 8, message = "パスワードは8文字以上必要です")
        String newPassword
) {}
