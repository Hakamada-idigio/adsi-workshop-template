# Unit 3: 打刻

## 概要

出勤・退勤・休憩開始・休憩終了の打刻 API を実装する。

## Phase

**Phase B**（Unit 1 完了後、Unit 2 と並列実装可能）

## ユーザーストーリー

- **US-001**: 社員として、出勤ボタンを押して出勤時刻を記録したい
- **US-002**: 社員として、退勤ボタンを押して退勤時刻を記録したい
- **US-003**: 社員として、休憩開始・終了ボタンを押して休憩時間を記録したい
- **US-018**: 社員として、月末に勤怠をまとめて提出したい（DRAFT → SUBMITTED）

## API エンドポイント

| メソッド | パス | 説明 |
|---------|------|------|
| POST | /api/attendance/clock-in | 出勤打刻 |
| POST | /api/attendance/clock-out | 退勤打刻 |
| POST | /api/attendance/break/start | 休憩開始 |
| POST | /api/attendance/break/end | 休憩終了 |
| POST | /api/attendance/submit | 月次勤怠提出 |

## 成果物

### ファイル

```
backend/src/main/java/com/example/attendance/
├── attendance/
│   ├── controller/
│   │   └── AttendanceController.java
│   ├── service/
│   │   ├── AttendanceService.java (interface)
│   │   └── AttendanceServiceImpl.java
│   └── dto/
│       ├── AttendanceRecordResponse.java (record)
│       └── BreakRecordResponse.java (record)
```

### ビジネスルール

| # | ルール | エラー時 |
|---|--------|---------|
| 1 | 同日に2回出勤打刻はできない | 409 Conflict |
| 2 | 出勤前に退勤・休憩はできない | 400 Bad Request |
| 3 | 既に退勤済みの日に再度退勤はできない | 400 Bad Request |
| 4 | 休憩中に休憩開始はできない | 400 Bad Request |
| 5 | 休憩中でないのに休憩終了はできない | 400 Bad Request |
| 6 | 勤務時間は1分単位で記録（丸めなし） | — |

### 処理フロー

**出勤打刻:**
1. JWT から社員ID を取得
2. 当日の AttendanceRecord が存在しないことを確認
3. 新規 AttendanceRecord を DRAFT ステータスで作成

**退勤打刻:**
1. 当日の AttendanceRecord を取得（存在しなければ 400）
2. clockOut が null であることを確認
3. 未終了の休憩があれば自動で休憩終了
4. clockOut を記録

**休憩開始:**
1. 当日の AttendanceRecord を取得（出勤済み・未退勤）
2. 未終了の BreakRecord がないことを確認
3. 新規 BreakRecord を作成

**休憩終了:**
1. 当日の未終了 BreakRecord を取得
2. breakEnd を記録

## テスト

- 出勤打刻成功 → 201 + レコード返却
- 出勤打刻失敗（既に出勤済み）→ 409
- 退勤打刻成功 → 200 + レコード返却
- 退勤打刻失敗（出勤していない）→ 400
- 休憩開始成功 → 201
- 休憩開始失敗（出勤していない）→ 400
- 休憩開始失敗（既に休憩中）→ 400
- 休憩終了成功 → 200
- 休憩終了失敗（休憩中でない）→ 400
- 退勤時に未終了休憩が自動終了される

## 依存

- Unit 1（AttendanceRecord / BreakRecord Entity + Repository）
