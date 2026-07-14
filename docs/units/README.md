# Unit of Work 分割 — Phase 1（MVP: 基本打刻）

## 依存図

```
Phase A: インターフェース定義（共有基盤）
   │
   ├── Unit 1: 共通基盤（Flyway・Entity・Enum・共通設定）
   │
   ▼
Phase B: 並列実装可能
   │
   ├── Unit 2: 認証（ログイン・JWT・パスワード変更）
   ├── Unit 3: 打刻（出勤・退勤・休憩開始・休憩終了）
   │
   ▼
Phase C: Phase B に依存
   │
   ├── Unit 4: 勤怠照会・修正（日別・月別一覧、打刻修正）
   │
   ▼
Phase D: 統合
   │
   └── Unit 5: フロントエンド統合（Next.js 画面実装）
```

## Phase 割り当て

| Phase | Unit | 依存先 | 並列可否 |
|-------|------|--------|---------|
| A | Unit 1: 共通基盤 | なし | — |
| B | Unit 2: 認証 | Unit 1 | Unit 3 と並列可 |
| B | Unit 3: 打刻 | Unit 1 | Unit 2 と並列可 |
| C | Unit 4: 勤怠照会・修正 | Unit 1, 3 | — |
| D | Unit 5: フロントエンド | Unit 2, 3, 4 | — |

## スコープ（Phase 1 MVP）

Phase 1 で実装する要求仕様のユーザーストーリー:

- US-001: 出勤打刻
- US-002: 退勤打刻
- US-003: 休憩開始・終了打刻
- US-004: 承認前の打刻修正
- US-006: 日別・月別勤怠一覧
- US-016: ログイン
- US-017: パスワード変更

## 技術スタック（Phase 1）

| レイヤー | 技術 |
|----------|------|
| Backend | Java 21 / Spring Boot 3.x |
| Frontend | TypeScript / Next.js 14 (App Router) |
| DB | PostgreSQL 16 |
| マイグレーション | Flyway |
| 認証 | JWT (jjwt) |
| テスト | JUnit 5 + Vitest + Testing Library |
