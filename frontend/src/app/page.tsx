"use client";

import { useEffect, useState } from "react";
import { useAuth } from "@/hooks/useAuth";
import { useAttendance } from "@/hooks/useAttendance";
import ClockButton from "@/components/ClockButton";
import { withBasePath } from "@/lib/api-client";

export default function DashboardPage() {
  const { user, loading: authLoading } = useAuth();
  const { dailyData, fetchDaily, clockIn, clockOut, breakStart, breakEnd, error } = useAttendance();
  const [currentTime, setCurrentTime] = useState(new Date());
  const [actionError, setActionError] = useState<string | null>(null);

  useEffect(() => {
    if (user) fetchDaily();
  }, [user, fetchDaily]);

  useEffect(() => {
    const timer = setInterval(() => setCurrentTime(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);

  const formatTime = (date: Date) =>
    date.toLocaleTimeString("ja-JP", { hour: "2-digit", minute: "2-digit", second: "2-digit" });

  const formatDate = (date: Date) =>
    date.toLocaleDateString("ja-JP", { year: "numeric", month: "long", day: "numeric", weekday: "long" });

  const formatDateTime = (dt: string | null) => {
    if (!dt) return "--:--";
    const d = new Date(dt);
    return d.toLocaleTimeString("ja-JP", { hour: "2-digit", minute: "2-digit" });
  };

  const formatMinutesToHM = (minutes: number | null) => {
    if (minutes === null || minutes === undefined) return "0時間0分";
    const h = Math.floor(minutes / 60);
    const m = minutes % 60;
    return `${h}時間${m}分`;
  };

  const handleAction = async (action: () => Promise<void>) => {
    setActionError(null);
    try {
      await action();
    } catch (err) {
      setActionError(err instanceof Error ? err.message : "エラーが発生しました");
    }
  };

  if (authLoading || !user) return null;

  const record = dailyData?.record ?? null;
  const isClockedIn = record !== null && record.clockOut === null;
  const isClockedOut = record !== null && record.clockOut !== null;
  const isOnBreak = dailyData?.isOnBreak ?? false;
  const totalBreakMinutes = record?.breaks?.reduce((sum, b) => sum + (b.durationMinutes ?? 0), 0) ?? 0;

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

      {(actionError || error) && (
        <div className="max-w-lg mx-auto bg-red-50 border border-red-200 rounded-xl p-3 text-sm text-red-600 font-medium text-center">
          {actionError || error}
        </div>
      )}

      <div className="grid grid-cols-2 gap-4 max-w-lg mx-auto">
        <ClockButton
          label="出勤"
          icon={<span>&#x2600;&#xFE0F;</span>}
          onClick={() => handleAction(clockIn)}
          variant="primary"
          disabled={isClockedIn || isClockedOut}
          time={record ? formatDateTime(record.clockIn) : undefined}
        />
        <ClockButton
          label="退勤"
          icon={<span>&#x1F319;</span>}
          onClick={() => handleAction(clockOut)}
          variant="danger"
          disabled={!isClockedIn}
          time={record?.clockOut ? formatDateTime(record.clockOut) : undefined}
        />
        <ClockButton
          label="休憩開始"
          icon={<span>&#x2615;</span>}
          onClick={() => handleAction(breakStart)}
          variant="secondary"
          disabled={!isClockedIn || isOnBreak}
        />
        <ClockButton
          label="休憩終了"
          icon={<span>&#x1F4AA;</span>}
          onClick={() => handleAction(breakEnd)}
          variant="accent"
          disabled={!isOnBreak}
        />
      </div>

      <div className="card-pop max-w-lg mx-auto">
        <h2 className="font-bold text-lg mb-4 flex items-center gap-2">
          <span>&#x1F4CB;</span> 本日の記録
        </h2>
        <div className="space-y-3">
          <div className="flex justify-between items-center py-2 border-b border-purple-50">
            <span className="text-gray-600">出勤</span>
            <span className="font-bold text-primary">{formatDateTime(record?.clockIn ?? null)}</span>
          </div>
          <div className="flex justify-between items-center py-2 border-b border-purple-50">
            <span className="text-gray-600">退勤</span>
            <span className="font-bold text-gray-400">{formatDateTime(record?.clockOut ?? null)}</span>
          </div>
          <div className="flex justify-between items-center py-2 border-b border-purple-50">
            <span className="text-gray-600">休憩</span>
            <span className="font-bold text-secondary">{totalBreakMinutes}分</span>
          </div>
          <div className="flex justify-between items-center py-2">
            <span className="text-gray-600 font-medium">実労働</span>
            <span className="font-extrabold text-xl text-primary">
              {formatMinutesToHM(record?.totalWorkingMinutes ?? null)}
            </span>
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
