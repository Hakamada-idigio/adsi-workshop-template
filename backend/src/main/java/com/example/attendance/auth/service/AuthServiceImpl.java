package com.example.attendance.auth.service;

import com.example.attendance.auth.dto.ChangePasswordRequest;
import com.example.attendance.auth.dto.LoginRequest;
import com.example.attendance.auth.dto.LoginResponse;
import com.example.attendance.auth.security.JwtTokenProvider;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.common.exception.ResourceNotFoundException;
import com.example.attendance.domain.repository.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final EmployeeRepository employeeRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(EmployeeRepository employeeRepository,
                           JwtTokenProvider jwtTokenProvider,
                           PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        var employee = employeeRepository.findByEmployeeNumber(request.employeeNumber())
                .orElseThrow(() -> {
                    log.warn("Login failed: employee number '{}' not found", request.employeeNumber());
                    return new BusinessException("社員番号またはパスワードが正しくありません");
                });

        if (!passwordEncoder.matches(request.password(), employee.getPassword())) {
            log.warn("Login failed: wrong password for employee '{}'", request.employeeNumber());
            throw new BusinessException("社員番号またはパスワードが正しくありません");
        }

        String token = jwtTokenProvider.generateToken(employee.getId(), employee.getEmployeeNumber());
        return new LoginResponse(token, employee.getName(), employee.getRole());
    }

    @Override
    @Transactional
    public void changePassword(Long employeeId, ChangePasswordRequest request) {
        var employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("社員が見つかりません"));

        if (!passwordEncoder.matches(request.currentPassword(), employee.getPassword())) {
            throw new BusinessException("現在のパスワードが正しくありません");
        }

        employee.setPassword(passwordEncoder.encode(request.newPassword()));
        employee.setUpdatedAt(LocalDateTime.now());
        employeeRepository.save(employee);
    }
}
