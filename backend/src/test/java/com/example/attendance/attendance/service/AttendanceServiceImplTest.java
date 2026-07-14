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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceImplTest {

    @Mock
    private AttendanceRecordRepository attendanceRecordRepository;

    @Mock
    private BreakRecordRepository breakRecordRepository;

    private AttendanceServiceImpl service;

    private static final Long EMPLOYEE_ID = 1L;

    @BeforeEach
    void setUp() {
        service = new AttendanceServiceImpl(attendanceRecordRepository, breakRecordRepository);
    }

    @Test
    @DisplayName("出勤打刻: 当日未打刻の場合、出勤時刻が記録される")
    void clockIn_notYetClockedIn_recordsClockInTime() {
        when(attendanceRecordRepository.findByEmployeeIdAndWorkDate(EMPLOYEE_ID, LocalDate.now()))
                .thenReturn(Optional.empty());
        when(attendanceRecordRepository.save(any(AttendanceRecord.class)))
                .thenAnswer(invocation -> {
                    AttendanceRecord r = invocation.getArgument(0);
                    r.setId(1L);
                    return r;
                });
        when(breakRecordRepository.findByAttendanceRecordId(1L))
                .thenReturn(List.of());

        AttendanceRecordResponse result = service.clockIn(EMPLOYEE_ID);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.employeeId()).isEqualTo(EMPLOYEE_ID);
        assertThat(result.workDate()).isEqualTo(LocalDate.now());
        assertThat(result.clockIn()).isNotNull();
        assertThat(result.status()).isEqualTo(AttendanceStatus.DRAFT);
    }

    @Test
    @DisplayName("出勤打刻: 既に出勤済みの場合、ConflictExceptionがスローされる")
    void clockIn_alreadyClockedIn_throwsConflict() {
        AttendanceRecord existing = AttendanceRecord.builder()
                .id(1L).employeeId(EMPLOYEE_ID).workDate(LocalDate.now())
                .clockIn(LocalDateTime.now()).status(AttendanceStatus.DRAFT)
                .build();
        when(attendanceRecordRepository.findByEmployeeIdAndWorkDate(EMPLOYEE_ID, LocalDate.now()))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.clockIn(EMPLOYEE_ID))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("退勤打刻: 出勤済み・未退勤の場合、退勤時刻が記録される")
    void clockOut_clockedIn_recordsClockOutTime() {
        AttendanceRecord record = AttendanceRecord.builder()
                .id(1L).employeeId(EMPLOYEE_ID).workDate(LocalDate.now())
                .clockIn(LocalDateTime.now().minusHours(8)).clockOut(null)
                .status(AttendanceStatus.DRAFT)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        when(attendanceRecordRepository.findByEmployeeIdAndWorkDate(EMPLOYEE_ID, LocalDate.now()))
                .thenReturn(Optional.of(record));
        when(breakRecordRepository.findByAttendanceRecordIdAndBreakEndIsNull(1L))
                .thenReturn(Optional.empty());
        when(attendanceRecordRepository.save(any(AttendanceRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(breakRecordRepository.findByAttendanceRecordId(1L))
                .thenReturn(List.of());

        AttendanceRecordResponse result = service.clockOut(EMPLOYEE_ID);

        assertThat(result.clockOut()).isNotNull();
    }

    @Test
    @DisplayName("退勤打刻: 出勤していない場合、BusinessExceptionがスローされる")
    void clockOut_notClockedIn_throwsException() {
        when(attendanceRecordRepository.findByEmployeeIdAndWorkDate(EMPLOYEE_ID, LocalDate.now()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.clockOut(EMPLOYEE_ID))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("退勤打刻: 既に退勤済みの場合、BusinessExceptionがスローされる")
    void clockOut_alreadyClockedOut_throwsException() {
        AttendanceRecord record = AttendanceRecord.builder()
                .id(1L).employeeId(EMPLOYEE_ID).workDate(LocalDate.now())
                .clockIn(LocalDateTime.now().minusHours(8))
                .clockOut(LocalDateTime.now())
                .status(AttendanceStatus.DRAFT)
                .build();
        when(attendanceRecordRepository.findByEmployeeIdAndWorkDate(EMPLOYEE_ID, LocalDate.now()))
                .thenReturn(Optional.of(record));

        assertThatThrownBy(() -> service.clockOut(EMPLOYEE_ID))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("退勤打刻: 未終了の休憩がある場合、自動で休憩終了される")
    void clockOut_withOpenBreak_autoEndsBreak() {
        AttendanceRecord record = AttendanceRecord.builder()
                .id(1L).employeeId(EMPLOYEE_ID).workDate(LocalDate.now())
                .clockIn(LocalDateTime.now().minusHours(8)).clockOut(null)
                .status(AttendanceStatus.DRAFT)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        BreakRecord openBreak = BreakRecord.builder()
                .id(10L).attendanceRecordId(1L)
                .breakStart(LocalDateTime.now().minusMinutes(30))
                .breakEnd(null)
                .createdAt(LocalDateTime.now())
                .build();

        when(attendanceRecordRepository.findByEmployeeIdAndWorkDate(EMPLOYEE_ID, LocalDate.now()))
                .thenReturn(Optional.of(record));
        when(breakRecordRepository.findByAttendanceRecordIdAndBreakEndIsNull(1L))
                .thenReturn(Optional.of(openBreak));
        when(breakRecordRepository.save(any(BreakRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(attendanceRecordRepository.save(any(AttendanceRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(breakRecordRepository.findByAttendanceRecordId(1L))
                .thenReturn(List.of(openBreak));

        service.clockOut(EMPLOYEE_ID);

        verify(breakRecordRepository).save(any(BreakRecord.class));
    }

    @Test
    @DisplayName("休憩開始: 出勤済み・未退勤・休憩中でない場合、休憩が開始される")
    void startBreak_valid_createsBreakRecord() {
        AttendanceRecord record = AttendanceRecord.builder()
                .id(1L).employeeId(EMPLOYEE_ID).workDate(LocalDate.now())
                .clockIn(LocalDateTime.now().minusHours(4)).clockOut(null)
                .status(AttendanceStatus.DRAFT)
                .build();
        when(attendanceRecordRepository.findByEmployeeIdAndWorkDate(EMPLOYEE_ID, LocalDate.now()))
                .thenReturn(Optional.of(record));
        when(breakRecordRepository.findByAttendanceRecordIdAndBreakEndIsNull(1L))
                .thenReturn(Optional.empty());
        when(breakRecordRepository.save(any(BreakRecord.class)))
                .thenAnswer(invocation -> {
                    BreakRecord b = invocation.getArgument(0);
                    b.setId(10L);
                    return b;
                });

        BreakRecordResponse result = service.startBreak(EMPLOYEE_ID);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.breakStart()).isNotNull();
        assertThat(result.breakEnd()).isNull();
    }

    @Test
    @DisplayName("休憩開始: 出勤していない場合、BusinessExceptionがスローされる")
    void startBreak_notClockedIn_throwsException() {
        when(attendanceRecordRepository.findByEmployeeIdAndWorkDate(EMPLOYEE_ID, LocalDate.now()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.startBreak(EMPLOYEE_ID))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("休憩開始: 既に休憩中の場合、BusinessExceptionがスローされる")
    void startBreak_alreadyOnBreak_throwsException() {
        AttendanceRecord record = AttendanceRecord.builder()
                .id(1L).employeeId(EMPLOYEE_ID).workDate(LocalDate.now())
                .clockIn(LocalDateTime.now().minusHours(4)).clockOut(null)
                .status(AttendanceStatus.DRAFT)
                .build();
        BreakRecord openBreak = BreakRecord.builder()
                .id(10L).attendanceRecordId(1L)
                .breakStart(LocalDateTime.now().minusMinutes(10))
                .breakEnd(null)
                .build();

        when(attendanceRecordRepository.findByEmployeeIdAndWorkDate(EMPLOYEE_ID, LocalDate.now()))
                .thenReturn(Optional.of(record));
        when(breakRecordRepository.findByAttendanceRecordIdAndBreakEndIsNull(1L))
                .thenReturn(Optional.of(openBreak));

        assertThatThrownBy(() -> service.startBreak(EMPLOYEE_ID))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("休憩終了: 休憩中の場合、休憩終了時刻が記録される")
    void endBreak_onBreak_recordsBreakEnd() {
        AttendanceRecord record = AttendanceRecord.builder()
                .id(1L).employeeId(EMPLOYEE_ID).workDate(LocalDate.now())
                .clockIn(LocalDateTime.now().minusHours(4)).clockOut(null)
                .status(AttendanceStatus.DRAFT)
                .build();
        BreakRecord openBreak = BreakRecord.builder()
                .id(10L).attendanceRecordId(1L)
                .breakStart(LocalDateTime.now().minusMinutes(30))
                .breakEnd(null)
                .createdAt(LocalDateTime.now())
                .build();

        when(attendanceRecordRepository.findByEmployeeIdAndWorkDate(EMPLOYEE_ID, LocalDate.now()))
                .thenReturn(Optional.of(record));
        when(breakRecordRepository.findByAttendanceRecordIdAndBreakEndIsNull(1L))
                .thenReturn(Optional.of(openBreak));
        when(breakRecordRepository.save(any(BreakRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BreakRecordResponse result = service.endBreak(EMPLOYEE_ID);

        assertThat(result.breakEnd()).isNotNull();
    }

    @Test
    @DisplayName("休憩終了: 休憩中でない場合、BusinessExceptionがスローされる")
    void endBreak_notOnBreak_throwsException() {
        AttendanceRecord record = AttendanceRecord.builder()
                .id(1L).employeeId(EMPLOYEE_ID).workDate(LocalDate.now())
                .clockIn(LocalDateTime.now().minusHours(4)).clockOut(null)
                .status(AttendanceStatus.DRAFT)
                .build();

        when(attendanceRecordRepository.findByEmployeeIdAndWorkDate(EMPLOYEE_ID, LocalDate.now()))
                .thenReturn(Optional.of(record));
        when(breakRecordRepository.findByAttendanceRecordIdAndBreakEndIsNull(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.endBreak(EMPLOYEE_ID))
                .isInstanceOf(BusinessException.class);
    }
}
