"use client";

import { useState } from "react";
import { apiFetch, withBasePath } from "@/lib/api-client";
import { LoginResponse, setToken, setStoredUser } from "@/lib/auth";

const animals = [
  { emoji: "\u{1F436}", top: "5%", left: "8%", size: "text-5xl", delay: "0s" },
  { emoji: "\u{1F431}", top: "12%", left: "75%", size: "text-6xl", delay: "0.5s" },
  { emoji: "\u{1F43C}", top: "25%", left: "3%", size: "text-4xl", delay: "1s" },
  { emoji: "\u{1F98A}", top: "35%", left: "85%", size: "text-5xl", delay: "1.5s" },
  { emoji: "\u{1F430}", top: "55%", left: "5%", size: "text-6xl", delay: "0.3s" },
  { emoji: "\u{1F428}", top: "65%", left: "80%", size: "text-5xl", delay: "0.8s" },
  { emoji: "\u{1F981}", top: "80%", left: "10%", size: "text-4xl", delay: "1.2s" },
  { emoji: "\u{1F438}", top: "75%", left: "88%", size: "text-5xl", delay: "0.6s" },
  { emoji: "\u{1F427}", top: "8%", left: "45%", size: "text-4xl", delay: "1.8s" },
  { emoji: "\u{1F984}", top: "45%", left: "90%", size: "text-6xl", delay: "0.2s" },
  { emoji: "\u{1F42F}", top: "90%", left: "50%", size: "text-5xl", delay: "1.1s" },
  { emoji: "\u{1F42E}", top: "20%", left: "90%", size: "text-4xl", delay: "0.9s" },
  { emoji: "\u{1F437}", top: "88%", left: "25%", size: "text-5xl", delay: "1.4s" },
  { emoji: "\u{1F435}", top: "40%", left: "2%", size: "text-4xl", delay: "0.7s" },
  { emoji: "\u{1F98B}", top: "15%", left: "30%", size: "text-4xl", delay: "1.6s" },
  { emoji: "\u{1F422}", top: "70%", left: "60%", size: "text-4xl", delay: "0.4s" },
];

export default function LoginPage() {
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
      window.location.href = withBasePath("/");
    } catch (err) {
      setError(err instanceof Error ? err.message : "ログインに失敗しました");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[80vh] flex items-center justify-center relative overflow-hidden">
      {/* Floating Animals Background */}
      {animals.map((animal, i) => (
        <span
          key={i}
          className={`absolute ${animal.size} animate-bounce opacity-70 select-none pointer-events-none`}
          style={{
            top: animal.top,
            left: animal.left,
            animationDelay: animal.delay,
            animationDuration: "3s",
          }}
        >
          {animal.emoji}
        </span>
      ))}

      <div className="w-full max-w-md relative z-10">
        <div className="text-center mb-8">
          <span className="text-6xl block mb-4">&#9200;</span>
          <h1 className="text-4xl font-extrabold bg-gradient-to-r from-primary via-accent to-secondary bg-clip-text text-transparent">
            AttendPop
          </h1>
          <p className="text-gray-500 mt-2">勤怠管理をもっと楽しく</p>
        </div>

        <form onSubmit={handleSubmit} className="card-pop space-y-5 backdrop-blur-sm bg-white/90">
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
            {loading ? "ログイン中..." : "ログイン \u{1F680}"}
          </button>
        </form>
      </div>
    </div>
  );
}
