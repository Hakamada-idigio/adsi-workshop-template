package com.example.attendance.attendance.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record MonthlySubmitRequest(
        @NotNull
        @Pattern(regexp = "\\d{4}-\\d{2}", message = "形式は YYYY-MM にしてください")
        String yearMonth
) {}
