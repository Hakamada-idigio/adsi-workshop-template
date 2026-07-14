#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
FRONTEND_DIR="$(dirname "$SCRIPT_DIR")"

# 既存プロセス停止
echo "[dev-sagemaker] Stopping existing processes..."
pkill -f "next start" 2>/dev/null || true
pkill -f "next dev" 2>/dev/null || true
pkill -f "sagemaker-proxy" 2>/dev/null || true
sleep 1

# 環境変数
export SAGEMAKER=1
export NEXT_PUBLIC_BASE_PATH="/codeeditor/default/absports/3000"

cd "$FRONTEND_DIR"

# ビルド
echo "[dev-sagemaker] Building Next.js..."
npx next build

# Next.js を 3001 で起動（127.0.0.1 バインド）
echo "[dev-sagemaker] Starting Next.js on :3001..."
npx next start -H 127.0.0.1 -p 3001 &
sleep 3

# 復元プロキシを 3000 で起動
echo "[dev-sagemaker] Starting proxy on :3000..."
node scripts/sagemaker-proxy.mjs &

echo ""
echo "========================================="
echo "  SageMaker preview ready!"
echo "  PORTS タブ → 3000 の地球儀 → URL の ports を absports に置換"
echo "========================================="

wait
