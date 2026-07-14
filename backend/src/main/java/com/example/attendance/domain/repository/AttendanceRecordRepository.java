package com.example.attendance.domain.repository;

import com.example.attendance.domain.model.AttendanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    Optional<AttendanceRecord> findByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);

    Page<AttendanceRecord> findByEmployeeIdAndWorkDateBetween(
            Long employeeId, LocalDate startDate, LocalDate endDate, Pageable pageable);
}
