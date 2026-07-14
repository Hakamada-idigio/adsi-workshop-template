package com.example.attendance.domain.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public record WorkDuration(
        int totalMinutes,
        int breakMinutes,
        int netWorkingMinutes
) {
    public static WorkDuration calculate(LocalDateTime clockIn,
                                         LocalDateTime clockOut,
                                         List<BreakRecord> breakRecords) {
        LocalDateTime effectiveEnd = clockOut != null ? clockOut : LocalDateTime.now();
        int total = (int) ChronoUnit.MINUTES.between(clockIn, effectiveEnd);

        int breakMins = breakRecords.stream()
                .mapToInt(b -> {
                    LocalDateTime end = b.getBreakEnd() != null ? b.getBreakEnd() : LocalDateTime.now();
                    return (int) ChronoUnit.MINUTES.between(b.getBreakStart(), end);
                })
                .sum();

        return new WorkDuration(total, breakMins, total - breakMins);
    }
}
