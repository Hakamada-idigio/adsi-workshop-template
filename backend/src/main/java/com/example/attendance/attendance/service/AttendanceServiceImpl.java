package com.example.attendance.attendance.service;

import com.example.attendance.attendance.dto.AttendanceRecordResponse;
import com.example.attendance.attendance.dto.BreakRecordResponse;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.common.exception.ConflictException;
import com.example.attendance.domain.model.AttendanceRecord;
import com.example.attendance.domain.model.AttendanceStatus;
import com.example.attendance.domain.model.BreakRecord;
import com.example.attendance.domain.repository.AttendanceRecordRepository;
import com.example.attendance.domain.repository.BreakRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final BreakRecordRepository breakRecordRepository;

    public AttendanceServiceImpl(AttendanceRecordRepository attendanceRecordRepository,
                                 BreakRecordRepository breakRecordRepository) {
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.breakRecordRepository = breakRecordRepository;
    }

    @Override
    public AttendanceRecordResponse clockIn(Long employeeId) {
        LocalDate today = LocalDate.now();
        attendanceRecordRepository.findByEmployeeIdAndWorkDate(employeeId, today)
                .ifPresent(r -> {
                    throw new ConflictException("既に出勤済みです");
                });

        LocalDateTime now = LocalDateTime.now();
        AttendanceRecord record = AttendanceRecord.builder()
                .employeeId(employeeId)
                .workDate(today)
                .clockIn(now)
                .status(AttendanceStatus.DRAFT)
                .createdAt(now)
                .updatedAt(now)
                .build();

        AttendanceRecord saved = attendanceRecordRepository.save(record);
        List<BreakRecord> breaks = breakRecordRepository.findByAttendanceRecordId(saved.getId());
        return AttendanceRecordResponse.from(saved, breaks);
    }

    @Override
    public AttendanceRecordResponse clockOut(Long employeeId) {
        LocalDate today = LocalDate.now();
        AttendanceRecord record = attendanceRecordRepository.findByEmployeeIdAndWorkDate(employeeId, today)
                .orElseThrow(() -> new BusinessException("出勤打刻がありません"));

        if (record.getClockOut() != null) {
            throw new BusinessException("既に退勤済みです");
        }

        breakRecordRepository.findByAttendanceRecordIdAndBreakEndIsNull(record.getId())
                .ifPresent(openBreak -> {
                    openBreak.setBreakEnd(LocalDateTime.now());
                    breakRecordRepository.save(openBreak);
                });

        record.setClockOut(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        AttendanceRecord saved = attendanceRecordRepository.save(record);
        List<BreakRecord> breaks = breakRecordRepository.findByAttendanceRecordId(saved.getId());
        return AttendanceRecordResponse.from(saved, breaks);
    }

    @Override
    public BreakRecordResponse startBreak(Long employeeId) {
        AttendanceRecord record = getTodayActiveRecord(employeeId);

        breakRecordRepository.findByAttendanceRecordIdAndBreakEndIsNull(record.getId())
                .ifPresent(b -> {
                    throw new BusinessException("既に休憩中です");
                });

        LocalDateTime now = LocalDateTime.now();
        BreakRecord breakRecord = BreakRecord.builder()
                .attendanceRecordId(record.getId())
                .breakStart(now)
                .createdAt(now)
                .build();

        BreakRecord saved = breakRecordRepository.save(breakRecord);
        return BreakRecordResponse.from(saved);
    }

    @Override
    public BreakRecordResponse endBreak(Long employeeId) {
        AttendanceRecord record = getTodayActiveRecord(employeeId);

        BreakRecord openBreak = breakRecordRepository.findByAttendanceRecordIdAndBreakEndIsNull(record.getId())
                .orElseThrow(() -> new BusinessException("休憩中ではありません"));

        openBreak.setBreakEnd(LocalDateTime.now());
        BreakRecord saved = breakRecordRepository.save(openBreak);
        return BreakRecordResponse.from(saved);
    }

    @Override
    public void submitMonthly(Long employeeId, String yearMonth) {
        String[] parts = yearMonth.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        var page = attendanceRecordRepository.findByEmployeeIdAndWorkDateBetween(
                employeeId, startDate, endDate, org.springframework.data.domain.Pageable.unpaged());

        List<AttendanceRecord> records = page.getContent();
        if (records.isEmpty()) {
            throw new BusinessException("提出対象の勤怠記録がありません");
        }

        records.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.DRAFT)
                .forEach(r -> {
                    r.setStatus(AttendanceStatus.SUBMITTED);
                    r.setUpdatedAt(LocalDateTime.now());
                    attendanceRecordRepository.save(r);
                });
    }

    private AttendanceRecord getTodayActiveRecord(Long employeeId) {
        LocalDate today = LocalDate.now();
        AttendanceRecord record = attendanceRecordRepository.findByEmployeeIdAndWorkDate(employeeId, today)
                .orElseThrow(() -> new BusinessException("出勤打刻がありません"));

        if (record.getClockOut() != null) {
            throw new BusinessException("既に退勤済みです");
        }
        return record;
    }
}
