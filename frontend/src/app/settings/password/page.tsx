"use client";

import { useState } from "react";
import { useAuth } from "@/hooks/useAuth";
import { apiFetch } from "@/lib/api-client";

export default function PasswordPage() {
  const { user, loading: authLoading } = useAuth();
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [message, setMessage] = useState<{ type: "success" | "error"; text: string } | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setMessage(null);

    if (newPassword !== confirmPassword) {
      setMessage({ type: "error", text: "新しいパスワードが一致しません" });
      return;
    }

    if (newPassword.length < 8) {
      setMessage({ type: "error", text: "パスワードは8文字以上必要です" });
      return;
    }

    setLoading(true);
    try {
      await apiFetch("/auth/password", {
        method: "PUT",
        body: JSON.stringify({ currentPassword, newPassword }),
      });
      setMessage({ type: "success", text: "パスワードを変更しました!" });
      setCurrentPassword("");
      setNewPassword("");
      setConfirmPassword("");
    } catch (err) {
      setMessage({ type: "error", text: err instanceof Error ? err.message : "変更に失敗しました" });
    } finally {
      setLoading(false);
    }
  };

  if (authLoading || !user) return null;

  return (
    <div className="max-w-md mx-auto space-y-6">
      <h1 className="text-2xl font-extrabold text-center bg-gradient-to-r from-primary to-accent bg-clip-text text-transparent">
        &#x1F512; パスワード変更
      </h1>

      <form onSubmit={handleSubmit} className="card-pop space-y-5">
        <div>
          <label className="block text-sm font-bold text-gray-700 mb-1">
            現在のパスワード
          </label>
          <input
            type="password"
            value={currentPassword}
            onChange={(e) => setCurrentPassword(e.target.value)}
            className="input-field"
            required
          />
        </div>

        <div>
          <label className="block text-sm font-bold text-gray-700 mb-1">
            新しいパスワード
          </label>
          <input
            type="password"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            className="input-field"
            placeholder="8文字以上"
            required
          />
        </div>

        <div>
          <label className="block text-sm font-bold text-gray-700 mb-1">
            新しいパスワード（確認）
          </label>
          <input
            type="password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            className="input-field"
            required
          />
        </div>

        {message && (
          <div
            className={`rounded-xl p-3 text-sm font-medium ${
              message.type === "success"
                ? "bg-green-50 border border-green-200 text-green-600"
                : "bg-red-50 border border-red-200 text-red-600"
            }`}
          >
            {message.text}
          </div>
        )}

        <button type="submit" disabled={loading} className="btn-primary w-full">
          {loading ? "変更中..." : "変更する"}
        </button>
      </form>
    </div>
  );
}
