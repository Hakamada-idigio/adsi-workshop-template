package com.example.attendance.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CorrectionRequest(
        LocalDateTime clockIn,
        LocalDateTime clockOut,
        @NotBlank(message = "修正理由は必須です")
        @Size(max = 500, message = "修正理由は500文字以内にしてください")
        String reason
) {}
