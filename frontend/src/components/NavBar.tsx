"use client";

import { clearAuth, getStoredUser } from "@/lib/auth";
import { withBasePath } from "@/lib/api-client";
import { useEffect, useState } from "react";

export default function NavBar() {
  const [user, setUser] = useState<{ name: string; role: string } | null>(null);
  const [menuOpen, setMenuOpen] = useState(false);

  useEffect(() => {
    setUser(getStoredUser());
  }, []);

  const handleLogout = () => {
    clearAuth();
    window.location.href = withBasePath("/login");
  };

  return (
    <nav className="bg-white/80 backdrop-blur-md border-b border-purple-100 sticky top-0 z-50">
      <div className="max-w-5xl mx-auto px-4 h-16 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <span className="text-2xl">&#9200;</span>
          <span className="font-extrabold text-xl bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
            AttendPop
          </span>
        </div>

        {user && (
          <div className="hidden md:flex items-center gap-4">
            <a href={withBasePath("/")} className="text-sm font-semibold text-gray-600 hover:text-primary transition-colors">
              ホーム
            </a>
            <a href={withBasePath("/attendance")} className="text-sm font-semibold text-gray-600 hover:text-primary transition-colors">
              勤怠一覧
            </a>
            <div className="flex items-center gap-2 ml-4">
              <div className="w-8 h-8 rounded-full bg-gradient-to-br from-primary to-accent flex items-center justify-center text-white text-xs font-bold">
                {user.name.charAt(0)}
              </div>
              <span className="text-sm font-medium">{user.name}</span>
            </div>
            <button
              onClick={handleLogout}
              className="text-xs text-gray-400 hover:text-danger transition-colors"
            >
              ログアウト
            </button>
          </div>
        )}

        {user && (
          <button
            className="md:hidden text-2xl"
            onClick={() => setMenuOpen(!menuOpen)}
          >
            {menuOpen ? "✕" : "☰"}
          </button>
        )}
      </div>

      {menuOpen && user && (
        <div className="md:hidden bg-white border-t border-purple-100 p-4 space-y-3">
          <a href={withBasePath("/")} className="block text-sm font-semibold text-gray-600">ホーム</a>
          <a href={withBasePath("/attendance")} className="block text-sm font-semibold text-gray-600">勤怠一覧</a>
          <div className="pt-2 border-t border-gray-100">
            <span className="text-sm text-gray-500">{user.name}</span>
          </div>
          <button onClick={handleLogout} className="text-sm text-danger font-medium">
            ログアウト
          </button>
        </div>
      )}
    </nav>
  );
}
