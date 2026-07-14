package com.example.attendance.attendance.service;

import com.example.attendance.attendance.dto.AttendanceDetailResponse;
import com.example.attendance.attendance.dto.AttendanceRecordResponse;
import com.example.attendance.attendance.dto.CorrectionRequest;
import com.example.attendance.attendance.dto.MonthlyAttendanceResponse;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.common.exception.ForbiddenException;
import com.example.attendance.domain.model.AttendanceRecord;
import com.example.attendance.domain.model.AttendanceStatus;
import com.example.attendance.domain.model.BreakRecord;
import com.example.attendance.domain.repository.AttendanceRecordRepository;
import com.example.attendance.domain.repository.BreakRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceQueryServiceImplTest {

    @Mock
    private AttendanceRecordRepository attendanceRecordRepository;

    @Mock
    private BreakRecordRepository breakRecordRepository;

    private AttendanceQueryServiceImpl service;

    private static final Long EMPLOYEE_ID = 1L;

    @BeforeEach
    void setUp() {
        service = new AttendanceQueryServiceImpl(attendanceRecordRepository, breakRecordRepository);
    }

    @Test
    @DisplayName("日別勤怠取得: 打刻ありの場合、実労働時間が正しく返る")
    void getDailyAttendance_withRecord_returnsCorrectDuration() {
        LocalDate date = LocalDate.of(2026, 7, 14);
        AttendanceRecord record = AttendanceRecord.builder()
                .id(1L).employeeId(EMPLOYEE_ID).workDate(date)
                .clockIn(LocalDateTime.of(2026, 7, 14, 9, 0))
                .clockOut(LocalDateTime.of(2026, 7, 14, 18, 0))
                .status(AttendanceStatus.DRAFT)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        BreakRecord breakRecord = BreakRecord.builder()
                .id(10L).attendanceRecordId(1L)
                .breakStart(LocalDateTime.of(2026, 7, 14, 12, 0))
                .breakEnd(LocalDateTime.of(2026, 7, 14, 13, 0))
                .createdAt(LocalDateTime.now())
                .build();

        when(attendanceRecordRepository.findByEmployeeIdAndWorkDate(EMPLOYEE_ID, date))
                .thenReturn(Optional.of(record));
        when(breakRecordRepository.findByAttendanceRecordId(1L))
                .thenReturn(List.of(breakRecord));

        AttendanceDetailResponse result = service.getDailyAttendance(EMPLOYEE_ID, date);

        assertThat(result.record()).isNotNull();
        assertThat(result.record().totalWorkingMinutes()).isEqualTo(480);
        assertThat(result.isOnBreak()).isFalse();
        assertThat(result.canEdit()).isTrue();
    }

    @Test
    @DisplayName("日別勤怠取得: 打刻なしの場合、recordがnullで返る")
    void getDailyAttendance_noRecord_returnsNullRecord() {
        LocalDate date = LocalDate.of(2026, 7, 14);
        when(attendanceRecordRepository.findByEmployeeIdAndWorkDate(EMPLOYEE_ID, date))
                .thenReturn(Optional.empty());

        AttendanceDetailResponse result = service.getDailyAttendance(EMPLOYEE_ID, date);

        assertThat(result.record()).isNull();
    }

    @Test
    @DisplayName("月別勤怠一覧: 合計・所定・過不足が正しく計算される")
    void getMonthlyAttendance_returnsCorrectSummary() {
        AttendanceRecord record1 = AttendanceRecord.builder()
                .id(1L).employeeId(EMPLOYEE_ID).workDate(LocalDate.of(2026, 7, 1))
                .clockIn(LocalDateTime.of(2026, 7, 1, 9, 0))
                .clockOut(LocalDateTime.of(2026, 7, 1, 18, 0))
                .status(AttendanceStatus.DRAFT)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        AttendanceRecord record2 = AttendanceRecord.builder()
                .id(2L).employeeId(EMPLOYEE_ID).workDate(LocalDate.of(2026, 7, 2))
                .clockIn(LocalDateTime.of(2026, 7, 2, 9, 0))
                .clockOut(LocalDateTime.of(2026, 7, 2, 18, 0))
                .status(AttendanceStatus.DRAFT)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        when(attendanceRecordRepository.findByEmployeeIdAndWorkDateBetween(
                eq(EMPLOYEE_ID), any(LocalDate.class), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(record1, record2)));
        when(breakRecordRepository.findByAttendanceRecordId(1L)).thenReturn(List.of());
        when(breakRecordRepository.findByAttendanceRecordId(2L)).thenReturn(List.of());

        MonthlyAttendanceResponse result = service.getMonthlyAttendance(EMPLOYEE_ID, "2026-07", 0, 31);

        assertThat(result.yearMonth()).isEqualTo("2026-07");
        assertThat(result.records()).hasSize(2);
        assertThat(result.totalWorkingMinutes()).isEqualTo(1080);
        assertThat(result.requiredMinutes()).isGreaterThan(0);
        assertThat(result.coreTimeStart()).isEqualTo("10:00");
        assertThat(result.coreTimeEnd()).isEqualTo("15:00");
    }

    @Test
    @DisplayName("打刻修正: DRAFTの場合、正常に修正される")
    void correctAttendance_draft_success() {
        AttendanceRecord record = AttendanceRecord.builder()
                .id(1L).employeeId(EMPLOYEE_ID).workDate(LocalDate.of(2026, 7, 14))
                .clockIn(LocalDateTime.of(2026, 7, 14, 9, 0))
                .clockOut(LocalDateTime.of(2026, 7, 14, 18, 0))
                .status(AttendanceStatus.DRAFT)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        when(attendanceRecordRepository.findById(1L)).thenReturn(Optional.of(record));
        when(attendanceRecordRepository.save(any(AttendanceRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(breakRecordRepository.findByAttendanceRecordId(1L)).thenReturn(List.of());

        CorrectionRequest request = new CorrectionRequest(
                LocalDateTime.of(2026, 7, 14, 8, 50),
                LocalDateTime.of(2026, 7, 14, 18, 0),
                "打刻忘れのため修正"
        );

        AttendanceRecordResponse result = service.correctAttendance(EMPLOYEE_ID, 1L, request);

        assertThat(result.clockIn()).isEqualTo(LocalDateTime.of(2026, 7, 14, 8, 50));
    }

    @Test
    @DisplayName("打刻修正: APPROVEDの場合、ForbiddenExceptionがスローされる")
    void correctAttendance_approved_throwsForbidden() {
        AttendanceRecord record = AttendanceRecord.builder()
                .id(1L).employeeId(EMPLOYEE_ID).workDate(LocalDate.of(2026, 7, 14))
                .clockIn(LocalDateTime.of(2026, 7, 14, 9, 0))
                .status(AttendanceStatus.APPROVED)
                .build();

        when(attendanceRecordRepository.findById(1L)).thenReturn(Optional.of(record));

        CorrectionRequest request = new CorrectionRequest(
                LocalDateTime.of(2026, 7, 14, 8, 50), null, "修正理由");

        assertThatThrownBy(() -> service.correctAttendance(EMPLOYEE_ID, 1L, request))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("打刻修正: 他人の勤怠の場合、ForbiddenExceptionがスローされる")
    void correctAttendance_otherEmployee_throwsForbidden() {
        AttendanceRecord record = AttendanceRecord.builder()
                .id(1L).employeeId(999L).workDate(LocalDate.of(2026, 7, 14))
                .clockIn(LocalDateTime.of(2026, 7, 14, 9, 0))
                .status(AttendanceStatus.DRAFT)
                .build();

        when(attendanceRecordRepository.findById(1L)).thenReturn(Optional.of(record));

        CorrectionRequest request = new CorrectionRequest(
                LocalDateTime.of(2026, 7, 14, 8, 50), null, "修正理由");

        assertThatThrownBy(() -> service.correctAttendance(EMPLOYEE_ID, 1L, request))
                .isInstanceOf(ForbiddenException.class);
    }
}
