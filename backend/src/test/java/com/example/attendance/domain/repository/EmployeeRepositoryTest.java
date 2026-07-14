package com.example.attendance.domain.repository;

import com.example.attendance.domain.model.Employee;
import com.example.attendance.domain.model.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    @DisplayName("社員を保存して社員番号で検索できる")
    void findByEmployeeNumber_existingNumber_returnsEmployee() {
        var employee = Employee.builder()
                .employeeNumber("EMP001")
                .name("田中太郎")
                .email("tanaka@example.com")
                .password("hashed_password")
                .role(Role.EMPLOYEE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        employeeRepository.save(employee);

        var result = employeeRepository.findByEmployeeNumber("EMP001");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("田中太郎");
        assertThat(result.get().getRole()).isEqualTo(Role.EMPLOYEE);
    }

    @Test
    @DisplayName("存在しない社員番号で検索すると空を返す")
    void findByEmployeeNumber_nonExisting_returnsEmpty() {
        var result = employeeRepository.findByEmployeeNumber("NOT_EXIST");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("管理者IDで部下を検索できる")
    void findByManagerId_existingManager_returnsSubordinates() {
        var manager = Employee.builder()
                .employeeNumber("MGR001")
                .name("山田花子")
                .email("yamada@example.com")
                .password("hashed_password")
                .role(Role.MANAGER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        manager = employeeRepository.save(manager);

        var subordinate = Employee.builder()
                .employeeNumber("EMP002")
                .name("佐藤次郎")
                .email("sato@example.com")
                .password("hashed_password")
                .role(Role.EMPLOYEE)
                .managerId(manager.getId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        employeeRepository.save(subordinate);

        List<Employee> result = employeeRepository.findByManagerId(manager.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("佐藤次郎");
    }
}
