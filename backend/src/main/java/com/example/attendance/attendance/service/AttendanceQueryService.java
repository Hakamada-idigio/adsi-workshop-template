package com.example.attendance.attendance.service;

import com.example.attendance.attendance.dto.AttendanceDetailResponse;
import com.example.attendance.attendance.dto.AttendanceRecordResponse;
import com.example.attendance.attendance.dto.CorrectionRequest;
import com.example.attendance.attendance.dto.MonthlyAttendanceResponse;

import java.time.LocalDate;

public interface AttendanceQueryService {

    AttendanceDetailResponse getDailyAttendance(Long employeeId, LocalDate date);

    MonthlyAttendanceResponse getMonthlyAttendance(Long employeeId, String yearMonth, int page, int size);

    AttendanceRecordResponse correctAttendance(Long employeeId, Long recordId, CorrectionRequest request);
}
