package com.example.attendance.auth.service;

import com.example.attendance.auth.dto.ChangePasswordRequest;
import com.example.attendance.auth.dto.LoginRequest;
import com.example.attendance.auth.security.JwtTokenProvider;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.domain.model.Employee;
import com.example.attendance.domain.model.Role;
import com.example.attendance.domain.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private PasswordEncoder passwordEncoder;
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder(10);
        authService = new AuthServiceImpl(employeeRepository, jwtTokenProvider, passwordEncoder);
    }

    @Test
    @DisplayName("正しい社員番号とパスワードでログインするとJWTが返る")
    void login_validCredentials_returnsToken() {
        var employee = createEmployee("EMP001", "田中太郎", passwordEncoder.encode("password123"));
        when(employeeRepository.findByEmployeeNumber("EMP001")).thenReturn(Optional.of(employee));
        when(jwtTokenProvider.generateToken(1L, "EMP001")).thenReturn("jwt-token");

        var result = authService.login(new LoginRequest("EMP001", "password123"));

        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.employeeName()).isEqualTo("田中太郎");
        assertThat(result.role()).isEqualTo(Role.EMPLOYEE);
    }

    @Test
    @DisplayName("存在しない社員番号でログインすると例外が発生する")
    void login_nonExistingEmployee_throwsException() {
        when(employeeRepository.findByEmployeeNumber("NOTEXIST")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("NOTEXIST", "password")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("社員番号またはパスワードが正しくありません");
    }

    @Test
    @DisplayName("パスワードが間違っているとログイン失敗する")
    void login_wrongPassword_throwsException() {
        var employee = createEmployee("EMP001", "田中太郎", passwordEncoder.encode("correct"));
        when(employeeRepository.findByEmployeeNumber("EMP001")).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> authService.login(new LoginRequest("EMP001", "wrong")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("社員番号またはパスワードが正しくありません");
    }

    @Test
    @DisplayName("正しい現パスワードで新パスワードに変更できる")
    void changePassword_validCurrentPassword_updatesPassword() {
        var employee = createEmployee("EMP001", "田中太郎", passwordEncoder.encode("oldpass"));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        authService.changePassword(1L, new ChangePasswordRequest("oldpass", "newpass123"));

        verify(employeeRepository).save(any(Employee.class));
        assertThat(passwordEncoder.matches("newpass123", employee.getPassword())).isTrue();
    }

    @Test
    @DisplayName("現パスワードが間違っていると変更できない")
    void changePassword_wrongCurrentPassword_throwsException() {
        var employee = createEmployee("EMP001", "田中太郎", passwordEncoder.encode("correct"));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() ->
                authService.changePassword(1L, new ChangePasswordRequest("wrong", "newpass123")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("現在のパスワードが正しくありません");
    }

    private Employee createEmployee(String number, String name, String hashedPassword) {
        return Employee.builder()
                .id(1L)
                .employeeNumber(number)
                .name(name)
                .email(number.toLowerCase() + "@example.com")
                .password(hashedPassword)
                .role(Role.EMPLOYEE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(0L)
                .build();
    }
}
