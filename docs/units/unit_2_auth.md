# Unit 2: 認証

## 概要

社員番号 + パスワードによるログイン、JWT トークン発行、パスワード変更を実装する。

## Phase

**Phase B**（Unit 1 完了後、Unit 3 と並列実装可能）

## ユーザーストーリー

- **US-016**: ユーザーとして、社員番号とパスワードでログインしたい
- **US-017**: ユーザーとして、パスワードを変更したい

## API エンドポイント

| メソッド | パス | 説明 |
|---------|------|------|
| POST | /api/auth/login | ログイン（JWT 発行） |
| PUT | /api/auth/password | パスワード変更 |

## 成果物

### ファイル

```
backend/src/main/java/com/example/attendance/
├── auth/
│   ├── controller/
│   │   └── AuthController.java
│   ├── service/
│   │   ├── AuthService.java
│   │   └── AuthServiceImpl.java
│   ├── dto/
│   │   ├── LoginRequest.java (record)
│   │   ├── LoginResponse.java (record)
│   │   └── ChangePasswordRequest.java (record)
│   └── security/
│       ├── JwtTokenProvider.java
│       ├── JwtAuthenticationFilter.java
│       └── SecurityConfig.java
```

### 処理フロー

**ログイン:**
1. 社員番号で Employee を検索
2. パスワードを BCrypt で検証
3. JWT トークンを生成して返却

**パスワード変更:**
1. JWT から社員ID を取得
2. 現在のパスワードを検証
3. 新パスワードを BCrypt でハッシュ化して保存

### セキュリティ設定

- `/api/auth/login` のみ認証不要
- その他の `/api/**` は JWT 必須
- パスワードは BCrypt（strength=10）
- JWT 有効期限: 8時間

## テスト

- ログイン成功 → JWT が返る
- ログイン失敗（存在しない社員番号）→ 401
- ログイン失敗（パスワード不一致）→ 401
- パスワード変更成功 → 204
- パスワード変更失敗（現パスワード不一致）→ 400
- JWT なしでのアクセス → 401
- 無効な JWT → 401

## 依存

- Unit 1（Employee Entity / EmployeeRepository）
