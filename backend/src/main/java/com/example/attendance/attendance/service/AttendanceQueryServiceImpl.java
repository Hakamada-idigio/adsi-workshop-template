package com.example.attendance.attendance.service;

import com.example.attendance.attendance.dto.AttendanceDetailResponse;
import com.example.attendance.attendance.dto.AttendanceRecordResponse;
import com.example.attendance.attendance.dto.CorrectionRequest;
import com.example.attendance.attendance.dto.MonthlyAttendanceResponse;
import com.example.attendance.common.exception.ForbiddenException;
import com.example.attendance.common.exception.ResourceNotFoundException;
import com.example.attendance.domain.model.AttendanceRecord;
import com.example.attendance.domain.model.AttendanceStatus;
import com.example.attendance.domain.model.BreakRecord;
import com.example.attendance.domain.model.WorkDuration;
import com.example.attendance.domain.repository.AttendanceRecordRepository;
import com.example.attendance.domain.repository.BreakRecordRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AttendanceQueryServiceImpl implements AttendanceQueryService {

    private static final int REQUIRED_MINUTES_PER_DAY = 435; // 7.25h = 435min
    private static final String CORE_TIME_START = "10:00";
    private static final String CORE_TIME_END = "15:00";

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final BreakRecordRepository breakRecordRepository;

    public AttendanceQueryServiceImpl(AttendanceRecordRepository attendanceRecordRepository,
                                      BreakRecordRepository breakRecordRepository) {
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.breakRecordRepository = breakRecordRepository;
    }

    @Override
    public AttendanceDetailResponse getDailyAttendance(Long employeeId, LocalDate date) {
        return attendanceRecordRepository.findByEmployeeIdAndWorkDate(employeeId, date)
                .map(record -> {
                    List<BreakRecord> breaks = breakRecordRepository.findByAttendanceRecordId(record.getId());
                    boolean isOnBreak = breaks.stream()
                            .anyMatch(b -> b.getBreakEnd() == null);
                    boolean canEdit = record.getStatus() == AttendanceStatus.DRAFT;
                    AttendanceRecordResponse response = AttendanceRecordResponse.from(record, breaks);
                    return new AttendanceDetailResponse(response, isOnBreak, canEdit);
                })
                .orElse(new AttendanceDetailResponse(null, false, false));
    }

    @Override
    public MonthlyAttendanceResponse getMonthlyAttendance(Long employeeId, String yearMonth, int page, int size) {
        String[] parts = yearMonth.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        var pageResult = attendanceRecordRepository.findByEmployeeIdAndWorkDateBetween(
                employeeId, startDate, endDate, PageRequest.of(page, size));

        List<AttendanceRecordResponse> records = pageResult.getContent().stream()
                .map(record -> {
                    List<BreakRecord> breaks = breakRecordRepository.findByAttendanceRecordId(record.getId());
                    return AttendanceRecordResponse.from(record, breaks);
                })
                .toList();

        int totalWorkingMinutes = records.stream()
                .filter(r -> r.totalWorkingMinutes() != null)
                .mapToInt(AttendanceRecordResponse::totalWorkingMinutes)
                .sum();

        int businessDays = countBusinessDays(startDate, endDate);
        int requiredMinutes = businessDays * REQUIRED_MINUTES_PER_DAY;
        int balance = totalWorkingMinutes - requiredMinutes;

        return new MonthlyAttendanceResponse(
                yearMonth, records, totalWorkingMinutes,
                requiredMinutes, balance, CORE_TIME_START, CORE_TIME_END);
    }

    @Override
    @Transactional
    public AttendanceRecordResponse correctAttendance(Long employeeId, Long recordId, CorrectionRequest request) {
        AttendanceRecord record = attendanceRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("勤怠記録が見つかりません"));

        if (!record.getEmployeeId().equals(employeeId)) {
            throw new ForbiddenException("他人の勤怠は修正できません");
        }

        if (record.getStatus() != AttendanceStatus.DRAFT) {
            throw new ForbiddenException("承認済みの勤怠は直接修正できません");
        }

        if (request.clockIn() != null) {
            record.setClockIn(request.clockIn());
        }
        if (request.clockOut() != null) {
            record.setClockOut(request.clockOut());
        }
        record.setUpdatedAt(LocalDateTime.now());

        AttendanceRecord saved = attendanceRecordRepository.save(record);
        List<BreakRecord> breaks = breakRecordRepository.findByAttendanceRecordId(saved.getId());
        return AttendanceRecordResponse.from(saved, breaks);
    }

    private int countBusinessDays(LocalDate start, LocalDate end) {
        int count = 0;
        LocalDate date = start;
        while (!date.isAfter(end)) {
            DayOfWeek dow = date.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                count++;
            }
            date = date.plusDays(1);
        }
        return count;
    }
}
