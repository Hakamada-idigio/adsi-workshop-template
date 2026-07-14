package com.example.attendance.attendance.controller;

import com.example.attendance.attendance.dto.AttendanceRecordResponse;
import com.example.attendance.attendance.dto.BreakRecordResponse;
import com.example.attendance.attendance.service.AttendanceService;
import com.example.attendance.auth.security.JwtAuthenticationFilter;
import com.example.attendance.auth.security.JwtTokenProvider;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.common.exception.ConflictException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.example.attendance.domain.model.AttendanceStatus;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AttendanceController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AttendanceService attendanceService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final Long EMPLOYEE_ID = 1L;

    @Test
    @DisplayName("POST /attendance/clock-in: 成功時201を返す")
    void clockIn_success_returns201() throws Exception {
        var response = new AttendanceRecordResponse(
                1L, EMPLOYEE_ID, LocalDate.now(), LocalDateTime.now(),
                null, AttendanceStatus.DRAFT, List.of(), null);
        when(attendanceService.clockIn(EMPLOYEE_ID)).thenReturn(response);

        mockMvc.perform(post("/attendance/clock-in")
                        .header("X-Employee-Id", EMPLOYEE_ID))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    @DisplayName("POST /attendance/clock-in: 既に出勤済みなら409を返す")
    void clockIn_conflict_returns409() throws Exception {
        when(attendanceService.clockIn(EMPLOYEE_ID))
                .thenThrow(new ConflictException("既に出勤済みです"));

        mockMvc.perform(post("/attendance/clock-in")
                        .header("X-Employee-Id", EMPLOYEE_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    @DisplayName("POST /attendance/clock-out: 成功時200を返す")
    void clockOut_success_returns200() throws Exception {
        var response = new AttendanceRecordResponse(
                1L, EMPLOYEE_ID, LocalDate.now(),
                LocalDateTime.now().minusHours(8), LocalDateTime.now(),
                AttendanceStatus.DRAFT, List.of(), 480);
        when(attendanceService.clockOut(EMPLOYEE_ID)).thenReturn(response);

        mockMvc.perform(post("/attendance/clock-out")
                        .header("X-Employee-Id", EMPLOYEE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clockOut").isNotEmpty())
                .andExpect(jsonPath("$.totalWorkingMinutes").value(480));
    }

    @Test
    @DisplayName("POST /attendance/clock-out: 出勤していない場合400を返す")
    void clockOut_notClockedIn_returns400() throws Exception {
        when(attendanceService.clockOut(EMPLOYEE_ID))
                .thenThrow(new BusinessException("出勤打刻がありません"));

        mockMvc.perform(post("/attendance/clock-out")
                        .header("X-Employee-Id", EMPLOYEE_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("POST /attendance/break/start: 成功時201を返す")
    void startBreak_success_returns201() throws Exception {
        var response = new BreakRecordResponse(10L, LocalDateTime.now(), null, null);
        when(attendanceService.startBreak(EMPLOYEE_ID)).thenReturn(response);

        mockMvc.perform(post("/attendance/break/start")
                        .header("X-Employee-Id", EMPLOYEE_ID))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @DisplayName("POST /attendance/break/start: 出勤していない場合400を返す")
    void startBreak_notClockedIn_returns400() throws Exception {
        when(attendanceService.startBreak(EMPLOYEE_ID))
                .thenThrow(new BusinessException("出勤打刻がありません"));

        mockMvc.perform(post("/attendance/break/start")
                        .header("X-Employee-Id", EMPLOYEE_ID))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /attendance/break/start: 既に休憩中の場合400を返す")
    void startBreak_alreadyOnBreak_returns400() throws Exception {
        when(attendanceService.startBreak(EMPLOYEE_ID))
                .thenThrow(new BusinessException("既に休憩中です"));

        mockMvc.perform(post("/attendance/break/start")
                        .header("X-Employee-Id", EMPLOYEE_ID))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /attendance/break/end: 成功時200を返す")
    void endBreak_success_returns200() throws Exception {
        var response = new BreakRecordResponse(10L, LocalDateTime.now().minusMinutes(30),
                LocalDateTime.now(), 30);
        when(attendanceService.endBreak(EMPLOYEE_ID)).thenReturn(response);

        mockMvc.perform(post("/attendance/break/end")
                        .header("X-Employee-Id", EMPLOYEE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.durationMinutes").value(30));
    }

    @Test
    @DisplayName("POST /attendance/break/end: 休憩中でない場合400を返す")
    void endBreak_notOnBreak_returns400() throws Exception {
        when(attendanceService.endBreak(EMPLOYEE_ID))
                .thenThrow(new BusinessException("休憩中ではありません"));

        mockMvc.perform(post("/attendance/break/end")
                        .header("X-Employee-Id", EMPLOYEE_ID))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /attendance/submit: 成功時204を返す")
    void submitMonthly_success_returns204() throws Exception {
        mockMvc.perform(post("/attendance/submit")
                        .header("X-Employee-Id", EMPLOYEE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"yearMonth\":\"2026-07\"}"))
                .andExpect(status().isNoContent());
    }
}
