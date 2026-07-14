package com.example.attendance.auth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
                "test-secret-key-that-is-at-least-256-bits-long-for-hs256-algorithm",
                28800000L // 8 hours
        );
    }

    @Test
    @DisplayName("トークンを生成して社員IDを取得できる")
    void generateToken_validInput_canExtractEmployeeId() {
        String token = jwtTokenProvider.generateToken(1L, "EMP001");

        Long employeeId = jwtTokenProvider.getEmployeeId(token);

        assertThat(employeeId).isEqualTo(1L);
    }

    @Test
    @DisplayName("トークンから社員番号を取得できる")
    void generateToken_validInput_canExtractEmployeeNumber() {
        String token = jwtTokenProvider.generateToken(1L, "EMP001");

        String employeeNumber = jwtTokenProvider.getEmployeeNumber(token);

        assertThat(employeeNumber).isEqualTo("EMP001");
    }

    @Test
    @DisplayName("有効なトークンはバリデーション成功")
    void validateToken_validToken_returnsTrue() {
        String token = jwtTokenProvider.generateToken(1L, "EMP001");

        boolean result = jwtTokenProvider.validateToken(token);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("不正なトークンはバリデーション失敗")
    void validateToken_invalidToken_returnsFalse() {
        boolean result = jwtTokenProvider.validateToken("invalid.token.here");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("期限切れトークンはバリデーション失敗")
    void validateToken_expiredToken_returnsFalse() {
        var expiredProvider = new JwtTokenProvider(
                "test-secret-key-that-is-at-least-256-bits-long-for-hs256-algorithm",
                -1000L // already expired
        );
        String token = expiredProvider.generateToken(1L, "EMP001");

        boolean result = jwtTokenProvider.validateToken(token);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("nullトークンはバリデーション失敗")
    void validateToken_nullToken_returnsFalse() {
        boolean result = jwtTokenProvider.validateToken(null);

        assertThat(result).isFalse();
    }
}
