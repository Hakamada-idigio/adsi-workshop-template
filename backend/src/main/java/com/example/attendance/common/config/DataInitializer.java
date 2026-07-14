package com.example.attendance.common.config;

import com.example.attendance.domain.model.Employee;
import com.example.attendance.domain.model.Role;
import com.example.attendance.domain.repository.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@Profile("workshop")
public class DataInitializer implements ApplicationRunner {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (employeeRepository.count() > 0) {
            return;
        }

        String hashedPassword = passwordEncoder.encode("password123");

        var manager = employeeRepository.save(Employee.builder()
                .employeeNumber("MGR001")
                .name("山田花子")
                .email("yamada@example.com")
                .password(hashedPassword)
                .role(Role.MANAGER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        employeeRepository.save(Employee.builder()
                .employeeNumber("EMP001")
                .name("田中太郎")
                .email("tanaka@example.com")
                .password(hashedPassword)
                .role(Role.EMPLOYEE)
                .managerId(manager.getId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        log.info("Workshop test data initialized: MGR001/EMP001 (password: password123)");
    }
}
