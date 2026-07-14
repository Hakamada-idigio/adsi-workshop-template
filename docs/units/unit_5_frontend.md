# Unit 5: フロントエンド統合

## 概要

Next.js で Phase 1 の画面を実装する。ログイン、打刻ダッシュボード、勤怠一覧、パスワード変更。

## Phase

**Phase D**（全 Backend Unit 完了後に実装）

## ユーザーストーリー

- US-001〜004, US-006, US-016, US-017 の画面実装

## 画面

| # | 画面 | パス | 説明 |
|---|------|------|------|
| S-01 | ログイン | /login | 社員番号 + パスワード |
| S-02 | ダッシュボード | / | 打刻ボタン + 当日の状態 |
| S-03 | 日別勤怠詳細 | /attendance/[date] | 打刻修正 |
| S-04 | 月別勤怠一覧 | /attendance?month=YYYY-MM | 月間一覧 |
| S-05 | パスワード変更 | /settings/password | パスワード変更フォーム |

## 成果物

### ファイル構成

```
frontend/
├── src/
│   ├── app/
│   │   ├── layout.tsx
│   │   ├── page.tsx (ダッシュボード)
│   │   ├── login/
│   │   │   └── page.tsx
│   │   ├── attendance/
│   │   │   ├── page.tsx (月別一覧)
│   │   │   └── [date]/
│   │   │       └── page.tsx (日別詳細)
│   │   └── settings/
│   │       └── password/
│   │           └── page.tsx
│   ├── components/
│   │   ├── ClockButton.tsx
│   │   ├── AttendanceTable.tsx
│   │   ├── AttendanceCard.tsx
│   │   ├── MonthPicker.tsx
│   │   ├── StatusBadge.tsx
│   │   └── WorkTimeSummary.tsx
│   ├── lib/
│   │   ├── api-client.ts (fetch ラッパー + basePath 対応)
│   │   └── auth.ts (JWT 管理)
│   └── hooks/
│       ├── useAuth.ts
│       └── useAttendance.ts
├── next.config.js
├── tailwind.config.ts
└── package.json
```

### 主要コンポーネント

| コンポーネント | 役割 |
|--------------|------|
| ClockButton | 出勤/退勤/休憩ボタン（状態に応じて表示切替） |
| AttendanceTable | PC 用の月別勤怠テーブル |
| AttendanceCard | スマホ用の日別勤怠カード |
| MonthPicker | 年月ナビゲーション |
| StatusBadge | 状態バッジ（DRAFT/APPROVED 等） |
| WorkTimeSummary | 月間の労働時間サマリー（実働/所定/過不足） |

### API 通信

- `api-client.ts` で basePath 対応の fetch ラッパーを用意
- SageMaker 環境（`SAGEMAKER=1`）では `withBasePath()` で全パスを補正
- JWT は localStorage に保存し、fetch ヘッダーに自動付与
- 401 レスポンスでログイン画面にリダイレクト

### レスポンシブ

- Tailwind CSS でモバイルファーストに実装
- PC: サイドバー + メインコンテンツ
- スマホ: ハンバーガーメニュー + シングルカラム
- 打刻ボタンは min-h-16 で押しやすいサイズ

## テスト

- ログインフォーム表示・送信（Vitest + Testing Library）
- 打刻ボタンのクリックで API が呼ばれる
- 勤怠一覧のレンダリング
- 未ログイン時のリダイレクト
- レスポンシブ切り替え

## 依存

- Unit 2（認証 API）
- Unit 3（打刻 API）
- Unit 4（勤怠照会 API）
