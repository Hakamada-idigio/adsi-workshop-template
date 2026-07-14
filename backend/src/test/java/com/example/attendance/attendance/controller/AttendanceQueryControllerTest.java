package com.example.attendance.attendance.controller;

import com.example.attendance.attendance.dto.AttendanceDetailResponse;
import com.example.attendance.attendance.dto.AttendanceRecordResponse;
import com.example.attendance.attendance.dto.CorrectionRequest;
import com.example.attendance.attendance.dto.MonthlyAttendanceResponse;
import com.example.attendance.attendance.service.AttendanceQueryService;
import com.example.attendance.auth.security.JwtAuthenticationFilter;
import com.example.attendance.auth.security.JwtTokenProvider;
import com.example.attendance.common.exception.ForbiddenException;
import com.example.attendance.domain.model.AttendanceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AttendanceQueryController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class AttendanceQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AttendanceQueryService attendanceQueryService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final Long EMPLOYEE_ID = 1L;

    @BeforeEach
    void setUp() {
        var auth = new UsernamePasswordAuthenticationToken(
                EMPLOYEE_ID, "EMP001", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("GET /attendance/daily: 日別勤怠を取得できる")
    void getDailyAttendance_returns200() throws Exception {
        var record = new AttendanceRecordResponse(
                1L, EMPLOYEE_ID, LocalDate.of(2026, 7, 14),
                LocalDateTime.of(2026, 7, 14, 9, 0),
                LocalDateTime.of(2026, 7, 14, 18, 0),
                AttendanceStatus.DRAFT, List.of(), 480);
        var detail = new AttendanceDetailResponse(record, false, true);

        when(attendanceQueryService.getDailyAttendance(EMPLOYEE_ID, LocalDate.of(2026, 7, 14)))
                .thenReturn(detail);

        mockMvc.perform(get("/attendance/daily").param("date", "2026-07-14"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.record.totalWorkingMinutes").value(480))
                .andExpect(jsonPath("$.canEdit").value(true));
    }

    @Test
    @DisplayName("GET /attendance/monthly: 月別勤怠一覧を取得できる")
    void getMonthlyAttendance_returns200() throws Exception {
        var monthly = new MonthlyAttendanceResponse(
                "2026-07", List.of(), 9600, 10005, -405, "10:00", "15:00");

        when(attendanceQueryService.getMonthlyAttendance(EMPLOYEE_ID, "2026-07", 0, 31))
                .thenReturn(monthly);

        mockMvc.perform(get("/attendance/monthly").param("yearMonth", "2026-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.yearMonth").value("2026-07"))
                .andExpect(jsonPath("$.requiredMinutes").value(10005))
                .andExpect(jsonPath("$.coreTimeStart").value("10:00"));
    }

    @Test
    @DisplayName("PUT /attendance/{id}: DRAFT修正成功で200を返す")
    void correctAttendance_draft_returns200() throws Exception {
        var response = new AttendanceRecordResponse(
                1L, EMPLOYEE_ID, LocalDate.of(2026, 7, 14),
                LocalDateTime.of(2026, 7, 14, 8, 50),
                LocalDateTime.of(2026, 7, 14, 18, 0),
                AttendanceStatus.DRAFT, List.of(), 490);

        when(attendanceQueryService.correctAttendance(eq(EMPLOYEE_ID), eq(1L), any(CorrectionRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/attendance/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clockIn\":\"2026-07-14T08:50:00\",\"reason\":\"打刻忘れ修正\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clockIn").value("2026-07-14T08:50:00"));
    }

    @Test
    @DisplayName("PUT /attendance/{id}: APPROVED修正で403を返す")
    void correctAttendance_approved_returns403() throws Exception {
        when(attendanceQueryService.correctAttendance(eq(EMPLOYEE_ID), eq(1L), any(CorrectionRequest.class)))
                .thenThrow(new ForbiddenException("承認済みの勤怠は直接修正できません"));

        mockMvc.perform(put("/attendance/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clockIn\":\"2026-07-14T08:50:00\",\"reason\":\"修正理由\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("PUT /attendance/{id}: 理由なしで400を返す")
    void correctAttendance_noReason_returns400() throws Exception {
        mockMvc.perform(put("/attendance/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clockIn\":\"2026-07-14T08:50:00\",\"reason\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
