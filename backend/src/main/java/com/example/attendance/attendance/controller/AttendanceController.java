package com.example.attendance.attendance.controller;

import com.example.attendance.attendance.dto.AttendanceRecordResponse;
import com.example.attendance.attendance.dto.BreakRecordResponse;
import com.example.attendance.attendance.dto.MonthlySubmitRequest;
import com.example.attendance.attendance.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    private Long getEmployeeId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceRecordResponse> clockIn() {
        AttendanceRecordResponse response = attendanceService.clockIn(getEmployeeId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceRecordResponse> clockOut() {
        return ResponseEntity.ok(attendanceService.clockOut(getEmployeeId()));
    }

    @PostMapping("/break/start")
    public ResponseEntity<BreakRecordResponse> startBreak() {
        BreakRecordResponse response = attendanceService.startBreak(getEmployeeId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/break/end")
    public ResponseEntity<BreakRecordResponse> endBreak() {
        return ResponseEntity.ok(attendanceService.endBreak(getEmployeeId()));
    }

    @PostMapping("/submit")
    public ResponseEntity<Void> submitMonthly(@Valid @RequestBody MonthlySubmitRequest request) {
        attendanceService.submitMonthly(getEmployeeId(), request.yearMonth());
        return ResponseEntity.noContent().build();
    }
}
