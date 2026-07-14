package com.example.attendance.auth.service;

import com.example.attendance.auth.dto.ChangePasswordRequest;
import com.example.attendance.auth.dto.LoginRequest;
import com.example.attendance.auth.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    void changePassword(Long employeeId, ChangePasswordRequest request);
}
