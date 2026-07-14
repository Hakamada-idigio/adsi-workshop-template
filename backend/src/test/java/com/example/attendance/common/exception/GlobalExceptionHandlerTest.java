package com.example.attendance.common.exception;

import com.example.attendance.common.dto.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("BusinessExceptionは400 BAD_REQUESTを返す")
    void handleBusinessException_returns400() {
        var ex = new BusinessException("既に出勤済みです");

        var response = handler.handleBusinessException(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("BAD_REQUEST");
        assertThat(response.getBody().message()).isEqualTo("既に出勤済みです");
    }

    @Test
    @DisplayName("ResourceNotFoundExceptionは404 NOT_FOUNDを返す")
    void handleResourceNotFoundException_returns404() {
        var ex = new ResourceNotFoundException("勤怠記録が見つかりません");

        var response = handler.handleResourceNotFoundException(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("NOT_FOUND");
        assertThat(response.getBody().message()).isEqualTo("勤怠記録が見つかりません");
    }

    @Test
    @DisplayName("予期しない例外は500 INTERNAL_ERRORを返す")
    void handleUnexpectedException_returns500() {
        var ex = new RuntimeException("unexpected");

        var response = handler.handleUnexpectedException(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().message()).isEqualTo("サーバーエラーが発生しました");
    }
}
