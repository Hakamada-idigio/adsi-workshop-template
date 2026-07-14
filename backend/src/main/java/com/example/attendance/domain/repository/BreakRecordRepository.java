package com.example.attendance.domain.repository;

import com.example.attendance.domain.model.BreakRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BreakRecordRepository extends JpaRepository<BreakRecord, Long> {

    List<BreakRecord> findByAttendanceRecordId(Long attendanceRecordId);

    Optional<BreakRecord> findByAttendanceRecordIdAndBreakEndIsNull(Long attendanceRecordId);
}
