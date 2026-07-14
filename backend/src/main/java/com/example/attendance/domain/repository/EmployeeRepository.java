package com.example.attendance.domain.repository;

import com.example.attendance.domain.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeNumber(String employeeNumber);

    List<Employee> findByManagerId(Long managerId);
}
