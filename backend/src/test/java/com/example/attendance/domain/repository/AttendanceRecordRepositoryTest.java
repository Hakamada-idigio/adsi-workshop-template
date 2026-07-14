package com.example.attendance.domain.repository;

import com.example.attendance.domain.model.AttendanceRecord;
import com.example.attendance.domain.model.AttendanceStatus;
import com.example.attendance.domain.model.Employee;
import com.example.attendance.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AttendanceRecordRepositoryTest {

    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = employeeRepository.save(Employee.builder()
                .employeeNumber("EMP001")
                .name("田中太郎")
                .email("tanaka@example.com")
                .password("hashed_password")
                .role(Role.EMPLOYEE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
    }

    @Test
    @DisplayName("社員IDと勤務日で勤怠記録を検索できる")
    void findByEmployeeIdAndWorkDate_existing_returnsRecord() {
        var record = AttendanceRecord.builder()
                .employeeId(employee.getId())
                .workDate(LocalDate.of(2026, 7, 14))
                .clockIn(LocalDateTime.of(2026, 7, 14, 9, 0))
                .status(AttendanceStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        attendanceRecordRepository.save(record);

        var result = attendanceRecordRepository.findByEmployeeIdAndWorkDate(
                employee.getId(), LocalDate.of(2026, 7, 14));

        assertThat(result).isPresent();
        assertThat(result.get().getClockIn())
                .isEqualTo(LocalDateTime.of(2026, 7, 14, 9, 0));
        assertThat(result.get().getStatus()).isEqualTo(AttendanceStatus.DRAFT);
    }

    @Test
    @DisplayName("期間指定で勤怠記録を検索できる（ページネーション）")
    void findByEmployeeIdAndWorkDateBetween_returnsPagedRecords() {
        for (int day = 1; day <= 5; day++) {
            attendanceRecordRepository.save(AttendanceRecord.builder()
                    .employeeId(employee.getId())
                    .workDate(LocalDate.of(2026, 7, day))
                    .clockIn(LocalDateTime.of(2026, 7, day, 9, 0))
                    .clockOut(LocalDateTime.of(2026, 7, day, 18, 0))
                    .status(AttendanceStatus.DRAFT)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());
        }

        var page = attendanceRecordRepository.findByEmployeeIdAndWorkDateBetween(
                employee.getId(),
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31),
                PageRequest.of(0, 3));

        assertThat(page.getContent()).hasSize(3);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("存在しない日付で検索すると空を返す")
    void findByEmployeeIdAndWorkDate_nonExisting_returnsEmpty() {
        var result = attendanceRecordRepository.findByEmployeeIdAndWorkDate(
                employee.getId(), LocalDate.of(2026, 12, 31));

        assertThat(result).isEmpty();
    }
}
