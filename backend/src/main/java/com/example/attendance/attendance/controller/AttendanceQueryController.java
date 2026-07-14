package com.example.attendance.attendance.controller;

import com.example.attendance.attendance.dto.AttendanceDetailResponse;
import com.example.attendance.attendance.dto.AttendanceRecordResponse;
import com.example.attendance.attendance.dto.CorrectionRequest;
import com.example.attendance.attendance.dto.MonthlyAttendanceResponse;
import com.example.attendance.attendance.service.AttendanceQueryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/attendance")
public class AttendanceQueryController {

    private final AttendanceQueryService attendanceQueryService;

    public AttendanceQueryController(AttendanceQueryService attendanceQueryService) {
        this.attendanceQueryService = attendanceQueryService;
    }

    @GetMapping("/daily")
    public ResponseEntity<AttendanceDetailResponse> getDailyAttendance(
            @RequestParam(required = false) LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(attendanceQueryService.getDailyAttendance(getEmployeeId(), targetDate));
    }

    @GetMapping("/monthly")
    public ResponseEntity<MonthlyAttendanceResponse> getMonthlyAttendance(
            @RequestParam String yearMonth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "31") int size) {
        return ResponseEntity.ok(attendanceQueryService.getMonthlyAttendance(getEmployeeId(), yearMonth, page, size));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AttendanceRecordResponse> correctAttendance(
            @PathVariable Long id,
            @Valid @RequestBody CorrectionRequest request) {
        return ResponseEntity.ok(attendanceQueryService.correctAttendance(getEmployeeId(), id, request));
    }

    private Long getEmployeeId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (Long) auth.getPrincipal();
    }
}
