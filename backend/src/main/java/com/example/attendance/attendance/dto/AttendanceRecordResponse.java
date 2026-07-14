package com.example.attendance.attendance.dto;

import com.example.attendance.domain.model.AttendanceRecord;
import com.example.attendance.domain.model.AttendanceStatus;
import com.example.attendance.domain.model.BreakRecord;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public record AttendanceRecordResponse(
        Long id,
        Long employeeId,
        LocalDate workDate,
        LocalDateTime clockIn,
        LocalDateTime clockOut,
        AttendanceStatus status,
        List<BreakRecordResponse> breaks,
        Integer totalWorkingMinutes
) {
    public static AttendanceRecordResponse from(AttendanceRecord record, List<BreakRecord> breakRecords) {
        List<BreakRecordResponse> breakResponses = breakRecords.stream()
                .map(BreakRecordResponse::from)
                .toList();

        Integer totalMinutes = calculateWorkingMinutes(record, breakRecords);

        return new AttendanceRecordResponse(
                record.getId(),
                record.getEmployeeId(),
                record.getWorkDate(),
                record.getClockIn(),
                record.getClockOut(),
                record.getStatus(),
                breakResponses,
                totalMinutes
        );
    }

    private static Integer calculateWorkingMinutes(AttendanceRecord record, List<BreakRecord> breakRecords) {
        if (record.getClockIn() == null || record.getClockOut() == null) {
            return null;
        }
        long totalMinutes = ChronoUnit.MINUTES.between(record.getClockIn(), record.getClockOut());
        long breakMinutes = breakRecords.stream()
                .filter(b -> b.getBreakStart() != null && b.getBreakEnd() != null)
                .mapToLong(b -> ChronoUnit.MINUTES.between(b.getBreakStart(), b.getBreakEnd()))
                .sum();
        return (int) (totalMinutes - breakMinutes);
    }
}
