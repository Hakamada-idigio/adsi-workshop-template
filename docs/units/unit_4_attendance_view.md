# Unit 4: 勤怠照会・修正

## 概要

日別・月別の勤怠一覧取得と、承認前の打刻修正を実装する。

## Phase

**Phase C**（Unit 3 完了後に実装）

## ユーザーストーリー

- **US-004**: 社員として、承認前の打刻を自分で修正したい
- **US-006**: 社員として、自分の日別・月別の勤務実績を一覧で確認したい

## API エンドポイント

| メソッド | パス | 説明 |
|---------|------|------|
| GET | /api/attendance/daily?date={date} | 日別勤怠取得 |
| GET | /api/attendance/monthly?yearMonth={YYYY-MM} | 月別勤怠一覧 |
| PUT | /api/attendance/{id} | 打刻修正（承認前のみ直接修正） |

## 成果物

### ファイル

```
backend/src/main/java/com/example/attendance/
├── attendance/
│   ├── controller/
│   │   └── AttendanceQueryController.java
│   ├── service/
│   │   ├── AttendanceQueryService.java (interface)
│   │   └── AttendanceQueryServiceImpl.java
│   └── dto/
│       ├── AttendanceDetailResponse.java (record)
│       ├── MonthlyAttendanceResponse.java (record)
│       └── CorrectionRequest.java (record)
```

### ビジネスルール

| # | ルール | エラー時 |
|---|--------|---------|
| 1 | 自分の勤怠のみ閲覧可能 | 403 Forbidden |
| 2 | 承認前（DRAFT）の打刻は本人が直接修正可能 | — |
| 3 | 承認後（APPROVED）の修正は不可（Phase 2 で申請対応） | 403 Forbidden |
| 4 | 修正には理由が必須 | 400 Bad Request |

### 処理フロー

**日別勤怠取得:**
1. JWT から社員ID を取得
2. 指定日（省略時は当日）の AttendanceRecord + BreakRecord を取得
3. 実労働時間を計算して返却

**月別勤怠一覧:**
1. 指定年月の AttendanceRecord 一覧を取得
2. 各日の実労働時間を計算
3. 月合計実労働時間・所定労働時間・過不足を計算して返却

**打刻修正:**
1. AttendanceRecord を取得
2. 本人の勤怠であること・ステータスが DRAFT であることを確認
3. clockIn / clockOut を修正値で更新

### 勤務時間計算ロジック（Value Object: WorkDuration）

```
実労働時間 = (退勤時刻 - 出勤時刻) - 休憩合計時間
休憩合計 = Σ(breakEnd - breakStart)
```

- 退勤前（clockOut = null）の場合、現在時刻までの暫定値を返す
- 休憩中（breakEnd = null）の場合、現在時刻までの暫定休憩時間を含める

## テスト

- 日別勤怠取得 → 打刻時刻・休憩・実労働時間が正しい
- 月別勤怠一覧 → 全日分のレコード + 合計が正しい
- 退勤前の暫定実労働時間が正しい
- 打刻修正成功（DRAFT）→ 200
- 打刻修正失敗（APPROVED）→ 403
- 打刻修正失敗（他人の勤怠）→ 403
- 打刻修正失敗（理由なし）→ 400
- WorkDuration の計算が1分単位で正確

## 依存

- Unit 1（Entity / Repository）
- Unit 3（打刻で作成されたデータを照会する）
