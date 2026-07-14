# ドメインモデル設計

## Entity

### Employee（社員）

勤怠管理の主体。ログインアカウントでもある。

| フィールド | 型 | 説明 |
|-----------|------|------|
| id | Long | 内部 PK（自動採番） |
| employeeNumber | String | 社員番号（ログインID・ユニーク） |
| name | String | 氏名 |
| email | String | メールアドレス |
| password | String | BCrypt ハッシュ |
| role | Role | EMPLOYEE / MANAGER |
| managerId | Long | 上長の Employee ID（nullable） |
| createdAt | LocalDateTime | 作成日時 |
| updatedAt | LocalDateTime | 更新日時 |
| version | Long | 楽観ロック |

### AttendanceRecord（勤怠記録）

1日1社員につき1レコード。出退勤時刻を保持する。

| フィールド | 型 | 説明 |
|-----------|------|------|
| id | Long | PK |
| employeeId | Long | FK → Employee |
| workDate | LocalDate | 勤務日 |
| clockIn | LocalDateTime | 出勤時刻 |
| clockOut | LocalDateTime | 退勤時刻（nullable） |
| status | AttendanceStatus | DRAFT / SUBMITTED / APPROVED / REJECTED |
| createdAt | LocalDateTime | 作成日時 |
| updatedAt | LocalDateTime | 更新日時 |
| version | Long | 楽観ロック |

### BreakRecord（休憩記録）

1日に複数回の休憩を記録できる。

| フィールド | 型 | 説明 |
|-----------|------|------|
| id | Long | PK |
| attendanceRecordId | Long | FK → AttendanceRecord |
| breakStart | LocalDateTime | 休憩開始 |
| breakEnd | LocalDateTime | 休憩終了（nullable） |
| createdAt | LocalDateTime | 作成日時 |

### LeaveRequest（休暇・申請）

有給休暇・残業・休日出勤の申請。

| フィールド | 型 | 説明 |
|-----------|------|------|
| id | Long | PK |
| employeeId | Long | FK → Employee |
| type | LeaveType | PAID_LEAVE / OVERTIME / HOLIDAY_WORK |
| startDate | LocalDate | 開始日 |
| endDate | LocalDate | 終了日 |
| reason | String | 申請理由 |
| status | RequestStatus | PENDING / APPROVED / REJECTED |
| approverId | Long | 承認者 Employee ID（nullable） |
| approvedAt | LocalDateTime | 承認日時（nullable） |
| createdAt | LocalDateTime | 作成日時 |
| updatedAt | LocalDateTime | 更新日時 |
| version | Long | 楽観ロック |

### CorrectionRequest（打刻修正申請）

承認後の打刻修正申請。

| フィールド | 型 | 説明 |
|-----------|------|------|
| id | Long | PK |
| attendanceRecordId | Long | FK → AttendanceRecord |
| employeeId | Long | FK → Employee |
| correctedClockIn | LocalDateTime | 修正後出勤時刻（nullable） |
| correctedClockOut | LocalDateTime | 修正後退勤時刻（nullable） |
| reason | String | 修正理由 |
| status | RequestStatus | PENDING / APPROVED / REJECTED |
| approverId | Long | 承認者 Employee ID（nullable） |
| approvedAt | LocalDateTime | 承認日時（nullable） |
| createdAt | LocalDateTime | 作成日時 |
| version | Long | 楽観ロック |

## Value Object

### WorkDuration（勤務時間）

出勤〜退勤から休憩を差し引いた実労働時間を計算する。

- `totalMinutes`: 総勤務分数（1分単位）
- `breakMinutes`: 総休憩分数
- `netWorkingMinutes`: 実労働分数（= total - break）

### TimeRange（時間帯）

開始時刻〜終了時刻のペア。休憩や勤務時間帯の表現に使う。

## Enum

| Enum | 値 |
|------|-----|
| Role | EMPLOYEE, MANAGER |
| AttendanceStatus | DRAFT, SUBMITTED, APPROVED, REJECTED |
| RequestStatus | PENDING, APPROVED, REJECTED |
| LeaveType | PAID_LEAVE, OVERTIME, HOLIDAY_WORK |

## 関連図

```
Employee (1) ──── (N) AttendanceRecord (1) ──── (N) BreakRecord
    │                       │
    │                       └──── (N) CorrectionRequest
    │
    └──── (N) LeaveRequest
```

- Employee は複数の AttendanceRecord を持つ（日ごと）
- AttendanceRecord は複数の BreakRecord を持つ（1日に複数回休憩可）
- AttendanceRecord は複数の CorrectionRequest を持ちうる
- Employee は複数の LeaveRequest を持つ

## Service

### AttendanceService

- `clockIn(employeeId)`: 出勤打刻
- `clockOut(employeeId)`: 退勤打刻
- `startBreak(employeeId)`: 休憩開始
- `endBreak(employeeId)`: 休憩終了
- `correctRecord(attendanceRecordId, corrections)`: 打刻修正（承認前は直接、承認後は申請作成）
- `getMonthlyRecords(employeeId, yearMonth)`: 月別勤怠取得
- `getDailyRecord(employeeId, date)`: 日別勤怠取得

### ApprovalService

- `approveAttendance(attendanceRecordId, managerId)`: 勤怠承認
- `rejectAttendance(attendanceRecordId, managerId, reason)`: 勤怠差し戻し
- `approveCorrectionRequest(requestId, managerId)`: 修正申請承認
- `rejectCorrectionRequest(requestId, managerId, reason)`: 修正申請却下

### LeaveService

- `submitLeaveRequest(employeeId, type, dateRange, reason)`: 申請提出
- `approveLeaveRequest(requestId, managerId)`: 申請承認
- `rejectLeaveRequest(requestId, managerId, reason)`: 申請却下

### ReportService

- `getMonthlyReport(managerId, yearMonth)`: 月次集計レポート
- `exportCsv(managerId, yearMonth)`: CSV エクスポート
- `getOvertimeAlerts(managerId)`: 36協定アラート取得

### AuthService

- `login(employeeNumber, password)`: ログイン（JWT 発行）
- `changePassword(employeeId, oldPassword, newPassword)`: パスワード変更

## Repository

| Repository | 主なメソッド |
|-----------|-------------|
| EmployeeRepository | `findByEmployeeNumber`, `findByManagerId` |
| AttendanceRecordRepository | `findByEmployeeIdAndWorkDate`, `findByEmployeeIdAndWorkDateBetween` |
| BreakRecordRepository | `findByAttendanceRecordId` |
| LeaveRequestRepository | `findByEmployeeId`, `findByStatus` |
| CorrectionRequestRepository | `findByAttendanceRecordId`, `findByStatus` |
