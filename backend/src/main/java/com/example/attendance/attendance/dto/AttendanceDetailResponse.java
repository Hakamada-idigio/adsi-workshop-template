package com.example.attendance.attendance.dto;

public record AttendanceDetailResponse(
        AttendanceRecordResponse record,
        boolean isOnBreak,
        boolean canEdit
) {}
