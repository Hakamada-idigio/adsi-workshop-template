package com.example.attendance.attendance.controller;

import com.example.attendance.attendance.dto.AttendanceRecordResponse;
import com.example.attendance.attendance.dto.BreakRecordResponse;
import com.example.attendance.attendance.dto.MonthlySubmitRequest;
import com.example.attendance.attendance.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceRecordResponse> clockIn(
            @RequestHeader("X-Employee-Id") Long employeeId) {
        AttendanceRecordResponse response = attendanceService.clockIn(employeeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceRecordResponse> clockOut(
            @RequestHeader("X-Employee-Id") Long employeeId) {
        return ResponseEntity.ok(attendanceService.clockOut(employeeId));
    }

    @PostMapping("/break/start")
    public ResponseEntity<BreakRecordResponse> startBreak(
            @RequestHeader("X-Employee-Id") Long employeeId) {
        BreakRecordResponse response = attendanceService.startBreak(employeeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/break/end")
    public ResponseEntity<BreakRecordResponse> endBreak(
            @RequestHeader("X-Employee-Id") Long employeeId) {
        return ResponseEntity.ok(attendanceService.endBreak(employeeId));
    }

    @PostMapping("/submit")
    public ResponseEntity<Void> submitMonthly(
            @RequestHeader("X-Employee-Id") Long employeeId,
            @Valid @RequestBody MonthlySubmitRequest request) {
        attendanceService.submitMonthly(employeeId, request.yearMonth());
        return ResponseEntity.noContent().build();
    }
}
