package com.example.attendance.auth.controller;

import com.example.attendance.auth.dto.LoginResponse;
import com.example.attendance.auth.security.JwtTokenProvider;
import com.example.attendance.auth.service.AuthService;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.domain.model.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("POST /auth/login - 正しい認証情報でログイン成功")
    void login_validCredentials_returns200() throws Exception {
        var response = new LoginResponse("jwt-token", "田中太郎", Role.EMPLOYEE);
        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeNumber\":\"EMP001\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.employeeName").value("田中太郎"))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"));
    }

    @Test
    @DisplayName("POST /auth/login - 認証失敗で401")
    void login_invalidCredentials_returns401() throws Exception {
        when(authService.login(any()))
                .thenThrow(new BusinessException("社員番号またはパスワードが正しくありません"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeNumber\":\"EMP001\",\"password\":\"wrong\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("社員番号またはパスワードが正しくありません"));
    }

    @Test
    @DisplayName("POST /auth/login - バリデーションエラーで400")
    void login_emptyFields_returns400() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeNumber\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("PUT /auth/password - JWT付きでパスワード変更成功")
    void changePassword_authenticated_returns204() throws Exception {
        String token = jwtTokenProvider.generateToken(1L, "EMP001");
        doNothing().when(authService).changePassword(any(), any());

        mockMvc.perform(put("/auth/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"oldpass\",\"newPassword\":\"newpass123\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PUT /auth/password - 現パスワード間違いで400")
    void changePassword_wrongCurrent_returns400() throws Exception {
        String token = jwtTokenProvider.generateToken(1L, "EMP001");
        doThrow(new BusinessException("現在のパスワードが正しくありません"))
                .when(authService).changePassword(any(), any());

        mockMvc.perform(put("/auth/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"wrong\",\"newPassword\":\"newpass123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("現在のパスワードが正しくありません"));
    }

    @Test
    @DisplayName("PUT /auth/password - JWT無しで401")
    void changePassword_noAuth_returns401() throws Exception {
        mockMvc.perform(put("/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"old\",\"newPassword\":\"newpass123\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /auth/password - 新パスワードが短すぎると400")
    void changePassword_shortNewPassword_returns400() throws Exception {
        String token = jwtTokenProvider.generateToken(1L, "EMP001");

        mockMvc.perform(put("/auth/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"oldpass\",\"newPassword\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
}
