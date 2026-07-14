package com.example.attendance.auth.dto;

import com.example.attendance.domain.model.Role;

public record LoginResponse(
        String token,
        String employeeName,
        Role role
) {}
