"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { apiFetch, withBasePath } from "@/lib/api-client";
import { LoginResponse, setToken, setStoredUser } from "@/lib/auth";

export default function LoginPage() {
  const router = useRouter();
  const [employeeNumber, setEmployeeNumber] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const data = await apiFetch<LoginResponse>("/auth/login", {
        method: "POST",
        body: JSON.stringify({ employeeNumber, password }),
      });
      setToken(data.token);
      setStoredUser(data.employeeName, data.role);
      router.push(withBasePath("/"));
    } catch (err) {
      setError(err instanceof Error ? err.message : "ログインに失敗しました");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[80vh] flex items-center justify-center">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <span className="text-6xl block mb-4">&#9200;</span>
          <h1 className="text-4xl font-extrabold bg-gradient-to-r from-primary via-accent to-secondary bg-clip-text text-transparent">
            AttendPop
          </h1>
          <p className="text-gray-500 mt-2">勤怠管理をもっと楽しく</p>
        </div>

        <form onSubmit={handleSubmit} className="card-pop space-y-5">
          <div>
            <label className="block text-sm font-bold text-gray-700 mb-1">
              社員番号
            </label>
            <input
              type="text"
              value={employeeNumber}
              onChange={(e) => setEmployeeNumber(e.target.value)}
              className="input-field"
              placeholder="EMP001"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-bold text-gray-700 mb-1">
              パスワード
            </label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="input-field"
              placeholder="********"
              required
            />
          </div>

          {error && (
            <div className="bg-red-50 border border-red-200 rounded-xl p-3 text-sm text-red-600 font-medium">
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="btn-primary w-full text-lg"
          >
            {loading ? "ログイン中..." : "ログイン &#x1F680;"}
          </button>
        </form>
      </div>
    </div>
  );
}
