package com.example.attendance.attendance.service;

import com.example.attendance.attendance.dto.AttendanceRecordResponse;
import com.example.attendance.attendance.dto.BreakRecordResponse;

public interface AttendanceService {

    AttendanceRecordResponse clockIn(Long employeeId);

    AttendanceRecordResponse clockOut(Long employeeId);

    BreakRecordResponse startBreak(Long employeeId);

    BreakRecordResponse endBreak(Long employeeId);

    void submitMonthly(Long employeeId, String yearMonth);
}
