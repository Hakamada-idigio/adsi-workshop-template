"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { isAuthenticated, getStoredUser } from "@/lib/auth";
import { withBasePath } from "@/lib/api-client";
import ClockButton from "@/components/ClockButton";

export default function DashboardPage() {
  const router = useRouter();
  const [currentTime, setCurrentTime] = useState(new Date());
  const [user, setUser] = useState<{ name: string; role: string } | null>(null);
  const [clockedIn, setClockedIn] = useState(false);
  const [onBreak, setOnBreak] = useState(false);
  const [clockInTime, setClockInTime] = useState<string | null>(null);

  useEffect(() => {
    if (!isAuthenticated()) {
      router.push(withBasePath("/login"));
      return;
    }
    setUser(getStoredUser());
  }, [router]);

  useEffect(() => {
    const timer = setInterval(() => setCurrentTime(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);

  const formatTime = (date: Date) =>
    date.toLocaleTimeString("ja-JP", { hour: "2-digit", minute: "2-digit", second: "2-digit" });

  const formatDate = (date: Date) =>
    date.toLocaleDateString("ja-JP", { year: "numeric", month: "long", day: "numeric", weekday: "long" });

  const handleClockIn = () => {
    setClockedIn(true);
    setClockInTime(formatTime(new Date()));
  };

  const handleClockOut = () => {
    setClockedIn(false);
    setOnBreak(false);
    setClockInTime(null);
  };

  const handleBreakStart = () => {
    setOnBreak(true);
  };

  const handleBreakEnd = () => {
    setOnBreak(false);
  };

  if (!user) return null;

  return (
    <div className="space-y-8">
      <div className="text-center space-y-2">
        <p className="text-gray-500 font-medium">{formatDate(currentTime)}</p>
        <p className="text-6xl font-extrabold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent tabular-nums">
          {formatTime(currentTime)}
        </p>
        <p className="text-lg">
          おはよう、<span className="font-bold text-primary">{user.name}</span> さん
        </p>
      </div>

      <div className="grid grid-cols-2 gap-4 max-w-lg mx-auto">
        <ClockButton
          label="出勤"
          icon={<span>&#x2600;&#xFE0F;</span>}
          onClick={handleClockIn}
          variant="primary"
          disabled={clockedIn}
          time={clockInTime ?? undefined}
        />
        <ClockButton
          label="退勤"
          icon={<span>&#x1F319;</span>}
          onClick={handleClockOut}
          variant="danger"
          disabled={!clockedIn}
        />
        <ClockButton
          label="休憩開始"
          icon={<span>&#x2615;</span>}
          onClick={handleBreakStart}
          variant="secondary"
          disabled={!clockedIn || onBreak}
        />
        <ClockButton
          label="休憩終了"
          icon={<span>&#x1F4AA;</span>}
          onClick={handleBreakEnd}
          variant="accent"
          disabled={!onBreak}
        />
      </div>

      <div className="card-pop max-w-lg mx-auto">
        <h2 className="font-bold text-lg mb-4 flex items-center gap-2">
          <span>&#x1F4CB;</span> 本日の記録
        </h2>
        <div className="space-y-3">
          <div className="flex justify-between items-center py-2 border-b border-purple-50">
            <span className="text-gray-600">出勤</span>
            <span className="font-bold text-primary">{clockInTime ?? "--:--"}</span>
          </div>
          <div className="flex justify-between items-center py-2 border-b border-purple-50">
            <span className="text-gray-600">退勤</span>
            <span className="font-bold text-gray-400">--:--</span>
          </div>
          <div className="flex justify-between items-center py-2 border-b border-purple-50">
            <span className="text-gray-600">休憩</span>
            <span className="font-bold text-secondary">0分</span>
          </div>
          <div className="flex justify-between items-center py-2">
            <span className="text-gray-600 font-medium">実労働</span>
            <span className="font-extrabold text-xl text-primary">0時間0分</span>
          </div>
        </div>
      </div>

      <div className="flex justify-center gap-3">
        <a href={withBasePath("/attendance")} className="btn-secondary text-sm">
          &#x1F4C5; 月別一覧
        </a>
        <a
          href={withBasePath("/settings/password")}
          className="text-sm px-4 py-3 rounded-2xl border-2 border-purple-200 text-gray-600 font-bold hover:border-primary hover:text-primary transition-all"
        >
          &#x1F512; 設定
        </a>
      </div>
    </div>
  );
}
