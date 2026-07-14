package com.example.attendance.attendance.dto;

import com.example.attendance.domain.model.BreakRecord;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record BreakRecordResponse(
        Long id,
        LocalDateTime breakStart,
        LocalDateTime breakEnd,
        Integer durationMinutes
) {
    public static BreakRecordResponse from(BreakRecord record) {
        Integer duration = null;
        if (record.getBreakStart() != null && record.getBreakEnd() != null) {
            duration = (int) ChronoUnit.MINUTES.between(record.getBreakStart(), record.getBreakEnd());
        }
        return new BreakRecordResponse(
                record.getId(),
                record.getBreakStart(),
                record.getBreakEnd(),
                duration
        );
    }
}
