# DB 設計

## ER 図

```
┌──────────────┐       ┌─────────────────────┐       ┌───────────────┐
│  employees   │1    N │ attendance_records   │1    N │ break_records │
│──────────────│───────│─────────────────────│───────│───────────────│
│ id (PK)      │       │ id (PK)             │       │ id (PK)       │
│ employee_no  │       │ employee_id (FK)    │       │ attendance_   │
│ name         │       │ work_date           │       │   record_id   │
│ email        │       │ clock_in            │       │ break_start   │
│ password     │       │ clock_out           │       │ break_end     │
│ role         │       │ status              │       │ created_at    │
│ manager_id   │       │ created_at          │       └───────────────┘
│ created_at   │       │ updated_at          │
│ updated_at   │       │ version             │
│ version      │       └─────────────────────┘
└──────────────┘               │
       │                       │ 1
       │                       │
       │ 1                     │ N
       │               ┌──────────────────────┐
       │               │ correction_requests  │
       │               │──────────────────────│
       │               │ id (PK)              │
       │               │ attendance_record_id │
       │               │ employee_id (FK)     │
       │               │ corrected_clock_in   │
       │               │ corrected_clock_out  │
       │               │ reason               │
       │               │ status               │
       │               │ approver_id (FK)     │
       │               │ approved_at          │
       │               │ created_at           │
       │               │ version              │
       │               └──────────────────────┘
       │ 1
       │
       │ N
┌──────────────────┐
│ leave_requests   │
│──────────────────│
│ id (PK)          │
│ employee_id (FK) │
│ type             │
│ start_date       │
│ end_date         │
│ reason           │
│ status           │
│ approver_id (FK) │
│ approved_at      │
│ created_at       │
│ updated_at       │
│ version          │
└──────────────────┘
```

## テーブル定義

### employees

| カラム | 型 | 制約 | 説明 |
|--------|------|------|------|
| id | BIGSERIAL | PK | 内部ID |
| employee_number | VARCHAR(20) | UNIQUE NOT NULL | 社員番号（ログインID） |
| name | VARCHAR(100) | NOT NULL | 氏名 |
| email | VARCHAR(255) | UNIQUE NOT NULL | メールアドレス |
| password | VARCHAR(255) | NOT NULL | BCrypt ハッシュ |
| role | VARCHAR(20) | NOT NULL DEFAULT 'EMPLOYEE' | EMPLOYEE / MANAGER |
| manager_id | BIGINT | FK → employees(id), NULL | 上長ID |
| created_at | TIMESTAMP | NOT NULL DEFAULT NOW() | 作成日時 |
| updated_at | TIMESTAMP | NOT NULL DEFAULT NOW() | 更新日時 |
| version | BIGINT | NOT NULL DEFAULT 0 | 楽観ロック |

インデックス:
- `idx_employees_employee_number` ON (employee_number)
- `idx_employees_manager_id` ON (manager_id)

### attendance_records

| カラム | 型 | 制約 | 説明 |
|--------|------|------|------|
| id | BIGSERIAL | PK | |
| employee_id | BIGINT | FK → employees(id), NOT NULL | |
| work_date | DATE | NOT NULL | 勤務日 |
| clock_in | TIMESTAMP | NOT NULL | 出勤時刻 |
| clock_out | TIMESTAMP | NULL | 退勤時刻 |
| status | VARCHAR(20) | NOT NULL DEFAULT 'DRAFT' | DRAFT/SUBMITTED/APPROVED/REJECTED |
| created_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |
| updated_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |
| version | BIGINT | NOT NULL DEFAULT 0 | 楽観ロック |

制約:
- `uq_attendance_employee_date` UNIQUE (employee_id, work_date)

インデックス:
- `idx_attendance_employee_date` ON (employee_id, work_date)
- `idx_attendance_employee_status` ON (employee_id, status)

### break_records

| カラム | 型 | 制約 | 説明 |
|--------|------|------|------|
| id | BIGSERIAL | PK | |
| attendance_record_id | BIGINT | FK → attendance_records(id), NOT NULL | |
| break_start | TIMESTAMP | NOT NULL | 休憩開始 |
| break_end | TIMESTAMP | NULL | 休憩終了 |
| created_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |

インデックス:
- `idx_break_attendance_id` ON (attendance_record_id)

### leave_requests

| カラム | 型 | 制約 | 説明 |
|--------|------|------|------|
| id | BIGSERIAL | PK | |
| employee_id | BIGINT | FK → employees(id), NOT NULL | |
| type | VARCHAR(20) | NOT NULL | PAID_LEAVE/OVERTIME/HOLIDAY_WORK |
| start_date | DATE | NOT NULL | 開始日 |
| end_date | DATE | NOT NULL | 終了日 |
| reason | VARCHAR(500) | NOT NULL | 申請理由 |
| status | VARCHAR(20) | NOT NULL DEFAULT 'PENDING' | PENDING/APPROVED/REJECTED |
| approver_id | BIGINT | FK → employees(id), NULL | 承認者 |
| approved_at | TIMESTAMP | NULL | 承認日時 |
| created_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |
| updated_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |
| version | BIGINT | NOT NULL DEFAULT 0 | 楽観ロック |

インデックス:
- `idx_leave_employee_id` ON (employee_id)
- `idx_leave_status` ON (status)

### correction_requests

| カラム | 型 | 制約 | 説明 |
|--------|------|------|------|
| id | BIGSERIAL | PK | |
| attendance_record_id | BIGINT | FK → attendance_records(id), NOT NULL | |
| employee_id | BIGINT | FK → employees(id), NOT NULL | |
| corrected_clock_in | TIMESTAMP | NULL | 修正後出勤時刻 |
| corrected_clock_out | TIMESTAMP | NULL | 修正後退勤時刻 |
| reason | VARCHAR(500) | NOT NULL | 修正理由 |
| status | VARCHAR(20) | NOT NULL DEFAULT 'PENDING' | PENDING/APPROVED/REJECTED |
| approver_id | BIGINT | FK → employees(id), NULL | 承認者 |
| approved_at | TIMESTAMP | NULL | 承認日時 |
| created_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |
| version | BIGINT | NOT NULL DEFAULT 0 | 楽観ロック |

インデックス:
- `idx_correction_attendance_id` ON (attendance_record_id)
- `idx_correction_status` ON (status)

## データ保存ポリシー

- 勤怠データ（attendance_records, break_records）は **3年間保存**（労基法準拠）
- 3年超のデータはアーカイブまたは論理削除で対応（将来検討）
