package com.example.attendance.domain.repository;

import com.example.attendance.domain.model.AttendanceRecord;
import com.example.attendance.domain.model.AttendanceStatus;
import com.example.attendance.domain.model.BreakRecord;
import com.example.attendance.domain.model.Employee;
import com.example.attendance.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class BreakRecordRepositoryTest {

    @Autowired
    private BreakRecordRepository breakRecordRepository;

    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    private AttendanceRecord attendanceRecord;

    @BeforeEach
    void setUp() {
        var employee = employeeRepository.save(Employee.builder()
                .employeeNumber("EMP001")
                .name("田中太郎")
                .email("tanaka@example.com")
                .password("hashed_password")
                .role(Role.EMPLOYEE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        attendanceRecord = attendanceRecordRepository.save(AttendanceRecord.builder()
                .employeeId(employee.getId())
                .workDate(LocalDate.of(2026, 7, 14))
                .clockIn(LocalDateTime.of(2026, 7, 14, 9, 0))
                .status(AttendanceStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
    }

    @Test
    @DisplayName("勤怠記録IDで休憩記録を検索できる")
    void findByAttendanceRecordId_returnsBreakRecords() {
        breakRecordRepository.save(BreakRecord.builder()
                .attendanceRecordId(attendanceRecord.getId())
                .breakStart(LocalDateTime.of(2026, 7, 14, 12, 0))
                .breakEnd(LocalDateTime.of(2026, 7, 14, 13, 0))
                .createdAt(LocalDateTime.now())
                .build());

        var result = breakRecordRepository.findByAttendanceRecordId(attendanceRecord.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBreakStart())
                .isEqualTo(LocalDateTime.of(2026, 7, 14, 12, 0));
        assertThat(result.get(0).getBreakEnd())
                .isEqualTo(LocalDateTime.of(2026, 7, 14, 13, 0));
    }

    @Test
    @DisplayName("未終了の休憩記録を検索できる")
    void findByAttendanceRecordIdAndBreakEndIsNull_returnsOngoingBreak() {
        breakRecordRepository.save(BreakRecord.builder()
                .attendanceRecordId(attendanceRecord.getId())
                .breakStart(LocalDateTime.of(2026, 7, 14, 12, 0))
                .breakEnd(null)
                .createdAt(LocalDateTime.now())
                .build());

        var result = breakRecordRepository.findByAttendanceRecordIdAndBreakEndIsNull(
                attendanceRecord.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getBreakEnd()).isNull();
    }

    @Test
    @DisplayName("未終了の休憩がない場合は空を返す")
    void findByAttendanceRecordIdAndBreakEndIsNull_noOngoing_returnsEmpty() {
        breakRecordRepository.save(BreakRecord.builder()
                .attendanceRecordId(attendanceRecord.getId())
                .breakStart(LocalDateTime.of(2026, 7, 14, 12, 0))
                .breakEnd(LocalDateTime.of(2026, 7, 14, 13, 0))
                .createdAt(LocalDateTime.now())
                .build());

        var result = breakRecordRepository.findByAttendanceRecordIdAndBreakEndIsNull(
                attendanceRecord.getId());

        assertThat(result).isEmpty();
    }
}
