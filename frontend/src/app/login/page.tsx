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
      {/* Tiger Background */}
      <div className="absolute inset-0 flex items-center justify-center pointer-events-none opacity-20">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 800" fill="none" className="w-[600px] h-[600px]">
          <circle cx="400" cy="400" r="350" fill="#FFF3E0" opacity="0.3"/>
          <ellipse cx="400" cy="520" rx="180" ry="140" fill="#F57C00"/>
          <ellipse cx="400" cy="300" rx="150" ry="130" fill="#FF9800"/>
          <ellipse cx="300" cy="200" rx="40" ry="45" fill="#FF9800"/>
          <ellipse cx="300" cy="200" rx="25" ry="30" fill="#FFE0B2"/>
          <ellipse cx="500" cy="200" rx="40" ry="45" fill="#FF9800"/>
          <ellipse cx="500" cy="200" rx="25" ry="30" fill="#FFE0B2"/>
          <ellipse cx="400" cy="330" rx="90" ry="80" fill="#FFF8E1"/>
          <ellipse cx="355" cy="285" rx="25" ry="22" fill="white"/>
          <ellipse cx="445" cy="285" rx="25" ry="22" fill="white"/>
          <circle cx="358" cy="288" r="12" fill="#33691E"/>
          <circle cx="448" cy="288" r="12" fill="#33691E"/>
          <circle cx="361" cy="285" r="5" fill="black"/>
          <circle cx="451" cy="285" r="5" fill="black"/>
          <circle cx="363" cy="282" r="2.5" fill="white"/>
          <circle cx="453" cy="282" r="2.5" fill="white"/>
          <ellipse cx="400" cy="332" rx="12" ry="8" fill="#BF360C"/>
          <path d="M400 340 Q390 360 380 355" stroke="#5D4037" strokeWidth="2.5" fill="none" strokeLinecap="round"/>
          <path d="M400 340 Q410 360 420 355" stroke="#5D4037" strokeWidth="2.5" fill="none" strokeLinecap="round"/>
          <line x1="320" y1="320" x2="280" y2="310" stroke="#5D4037" strokeWidth="1.5" opacity="0.6"/>
          <line x1="320" y1="335" x2="275" y2="340" stroke="#5D4037" strokeWidth="1.5" opacity="0.6"/>
          <line x1="480" y1="320" x2="520" y2="310" stroke="#5D4037" strokeWidth="1.5" opacity="0.6"/>
          <line x1="480" y1="335" x2="525" y2="340" stroke="#5D4037" strokeWidth="1.5" opacity="0.6"/>
          <path d="M370 230 Q380 250 375 270" stroke="#BF360C" strokeWidth="6" fill="none" strokeLinecap="round"/>
          <path d="M395 220 Q400 245 398 265" stroke="#BF360C" strokeWidth="6" fill="none" strokeLinecap="round"/>
          <path d="M425 230 Q420 250 422 270" stroke="#BF360C" strokeWidth="6" fill="none" strokeLinecap="round"/>
          <path d="M310 270 Q320 290 315 310" stroke="#BF360C" strokeWidth="5" fill="none" strokeLinecap="round"/>
          <path d="M490 270 Q480 290 485 310" stroke="#BF360C" strokeWidth="5" fill="none" strokeLinecap="round"/>
          <ellipse cx="340" cy="640" rx="35" ry="25" fill="#FF9800"/>
          <ellipse cx="460" cy="640" rx="35" ry="25" fill="#FF9800"/>
          <ellipse cx="340" cy="645" rx="25" ry="15" fill="#FFE0B2"/>
          <ellipse cx="460" cy="645" rx="25" ry="15" fill="#FFE0B2"/>
          <path d="M560 500 Q620 480 630 420 Q640 360 600 380" stroke="#FF9800" strokeWidth="20" fill="none" strokeLinecap="round"/>
          <path d="M300 470 Q320 490 310 520" stroke="#BF360C" strokeWidth="7" fill="none" strokeLinecap="round"/>
          <path d="M500 470 Q480 490 490 520" stroke="#BF360C" strokeWidth="7" fill="none" strokeLinecap="round"/>
        </svg>
      </div>

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
