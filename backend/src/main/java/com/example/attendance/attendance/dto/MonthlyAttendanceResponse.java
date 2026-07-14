package com.example.attendance.attendance.dto;

import java.util.List;

public record MonthlyAttendanceResponse(
        String yearMonth,
        List<AttendanceRecordResponse> records,
        int totalWorkingMinutes,
        int requiredMinutes,
        int balanceMinutes,
        String coreTimeStart,
        String coreTimeEnd
) {}
