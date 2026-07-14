# Unit 1: 共通基盤

## 概要

全 Unit が依存する共通基盤を先に整備する。DB マイグレーション、Entity、Enum、Spring Boot プロジェクト構成、共通エラーハンドリングを含む。

## Phase

**Phase A**（最初に実装。他の全 Unit がこれに依存）

## 成果物

### プロジェクト構成

```
backend/
├── src/main/java/com/example/attendance/
│   ├── AttendanceApplication.java
│   ├── common/
│   │   ├── exception/
│   │   │   ├── BusinessException.java
│   │   │   ├── ResourceNotFoundException.java
│   │   │   └── GlobalExceptionHandler.java
│   │   └── config/
│   │       └── JpaConfig.java
│   ├── domain/
│   │   ├── model/
│   │   │   ├── Employee.java
│   │   │   ├── AttendanceRecord.java
│   │   │   ├── BreakRecord.java
│   │   │   ├── Role.java
│   │   │   └── AttendanceStatus.java
│   │   └── repository/
│   │       ├── EmployeeRepository.java
│   │       ├── AttendanceRecordRepository.java
│   │       └── BreakRecordRepository.java
│   └── ...
├── src/main/resources/
│   ├── application.yml
│   ├── application-test.yml
│   └── db/migration/
│       ├── V1__create_employees.sql
│       ├── V2__create_attendance_records.sql
│       └── V3__create_break_records.sql
└── build.gradle (or pom.xml)
```

### Flyway マイグレーション

- `V1__create_employees.sql` — employees テーブル
- `V2__create_attendance_records.sql` — attendance_records テーブル
- `V3__create_break_records.sql` — break_records テーブル

### Entity

- `Employee` — 社員（id, employeeNumber, name, email, password, role, managerId, version）
- `AttendanceRecord` — 勤怠記録（id, employeeId, workDate, clockIn, clockOut, status, version）
- `BreakRecord` — 休憩記録（id, attendanceRecordId, breakStart, breakEnd）

### Enum

- `Role` — EMPLOYEE, MANAGER
- `AttendanceStatus` — DRAFT, SUBMITTED, APPROVED, REJECTED

### Repository（interface）

- `EmployeeRepository`
- `AttendanceRecordRepository`
- `BreakRecordRepository`

### 共通エラーハンドリング

- `GlobalExceptionHandler`（@RestControllerAdvice）
- `BusinessException` — 業務エラー（400系）
- `ResourceNotFoundException` — 存在しないリソース（404）

## テスト

- Flyway マイグレーションが正常に実行されること（@DataJpaTest）
- Entity の保存・取得ができること
- Repository の基本操作が動作すること

## 依存

なし（最初に実装）
