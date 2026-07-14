"use client";

import { useEffect, useState, useCallback } from "react";
import { useAuth } from "@/hooks/useAuth";
import { useAttendance } from "@/hooks/useAttendance";
import { MonthlyAttendanceResponse } from "@/lib/types";
import StatusBadge from "@/components/StatusBadge";

export default function AttendancePage() {
  const { user, loading: authLoading } = useAuth();
  const { fetchMonthly, submitMonthly } = useAttendance();
  const [monthlyData, setMonthlyData] = useState<MonthlyAttendanceResponse | null>(null);
  const [currentMonth, setCurrentMonth] = useState(() => {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [submitMessage, setSubmitMessage] = useState<string | null>(null);

  const loadMonthly = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchMonthly(currentMonth);
      setMonthlyData(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "取得に失敗しました");
    } finally {
      setLoading(false);
    }
  }, [currentMonth, fetchMonthly]);

  useEffect(() => {
    if (user) loadMonthly();
  }, [user, loadMonthly]);

  const changeMonth = (delta: number) => {
    const [y, m] = currentMonth.split("-").map(Number);
    const date = new Date(y, m - 1 + delta, 1);
    setCurrentMonth(`${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`);
  };

  const handleSubmit = async () => {
    setSubmitMessage(null);
    try {
      await submitMonthly(currentMonth);
      setSubmitMessage("月次提出しました");
      await loadMonthly();
    } catch (err) {
      setSubmitMessage(err instanceof Error ? err.message : "提出に失敗しました");
    }
  };

  const formatMinutes = (minutes: number | null) => {
    if (minutes === null || minutes === undefined) return "-";
    const h = Math.floor(Math.abs(minutes) / 60);
    const m = Math.abs(minutes) % 60;
    const sign = minutes < 0 ? "-" : "";
    return `${sign}${h}:${String(m).padStart(2, "0")}`;
  };

  const formatTime = (dt: string | null) => {
    if (!dt) return "-";
    return new Date(dt).toLocaleTimeString("ja-JP", { hour: "2-digit", minute: "2-digit" });
  };

  const displayMonth = () => {
    const [y, m] = currentMonth.split("-").map(Number);
    return `${y}年${m}月`;
  };

  const getDayOfWeek = (dateStr: string) => {
    const days = ["日", "月", "火", "水", "木", "金", "土"];
    return days[new Date(dateStr).getDay()];
  };

  if (authLoading || !user) return null;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-center gap-4">
        <button onClick={() => changeMonth(-1)} className="text-2xl text-primary hover:scale-110 transition-transform">
          &#x25C0;
        </button>
        <h1 className="text-2xl font-extrabold bg-gradient-to-r from-primary to-accent bg-clip-text text-transparent">
          {displayMonth()}
        </h1>
        <button onClick={() => changeMonth(1)} className="text-2xl text-primary hover:scale-110 transition-transform">
          &#x25B6;
        </button>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 rounded-xl p-3 text-sm text-red-600 font-medium text-center">
          {error}
        </div>
      )}

      {monthlyData && (
        <div className="card-pop">
          <div className="grid grid-cols-3 gap-4 text-center">
            <div>
              <p className="text-xs text-gray-500 font-medium">実労働</p>
              <p className="text-2xl font-extrabold text-primary">
                {formatMinutes(monthlyData.totalWorkingMinutes)}
              </p>
            </div>
            <div>
              <p className="text-xs text-gray-500 font-medium">所定</p>
              <p className="text-2xl font-extrabold text-gray-600">
                {formatMinutes(monthlyData.requiredMinutes)}
              </p>
            </div>
            <div>
              <p className="text-xs text-gray-500 font-medium">過不足</p>
              <p className={`text-2xl font-extrabold ${monthlyData.balanceMinutes >= 0 ? "text-success" : "text-danger"}`}>
                {formatMinutes(monthlyData.balanceMinutes)}
              </p>
            </div>
          </div>
          {monthlyData.coreTimeStart && (
            <p className="text-xs text-gray-400 text-center mt-2">
              コアタイム: {monthlyData.coreTimeStart}〜{monthlyData.coreTimeEnd}
            </p>
          )}
        </div>
      )}

      {loading ? (
        <div className="text-center text-gray-400 py-8">読み込み中...</div>
      ) : monthlyData && monthlyData.records.length > 0 ? (
        <>
          {/* Desktop Table */}
          <div className="card hidden md:block overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b-2 border-purple-100">
                  <th className="py-3 px-2 text-left font-bold text-gray-500">日付</th>
                  <th className="py-3 px-2 text-center font-bold text-gray-500">出勤</th>
                  <th className="py-3 px-2 text-center font-bold text-gray-500">退勤</th>
                  <th className="py-3 px-2 text-center font-bold text-gray-500">休憩</th>
                  <th className="py-3 px-2 text-center font-bold text-gray-500">実働</th>
                  <th className="py-3 px-2 text-center font-bold text-gray-500">状態</th>
                </tr>
              </thead>
              <tbody>
                {monthlyData.records.map((record) => {
                  const breakMin = record.breaks.reduce((s, b) => s + (b.durationMinutes ?? 0), 0);
                  const day = new Date(record.workDate).getDate();
                  return (
                    <tr key={record.id} className="border-b border-purple-50 hover:bg-purple-50/50 transition-colors">
                      <td className="py-3 px-2 font-medium">
                        {day}({getDayOfWeek(record.workDate)})
                      </td>
                      <td className="py-3 px-2 text-center font-mono">{formatTime(record.clockIn)}</td>
                      <td className="py-3 px-2 text-center font-mono">{formatTime(record.clockOut)}</td>
                      <td className="py-3 px-2 text-center">{breakMin}分</td>
                      <td className="py-3 px-2 text-center font-bold">{formatMinutes(record.totalWorkingMinutes)}</td>
                      <td className="py-3 px-2 text-center">
                        <StatusBadge status={record.status} />
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>

          {/* Mobile Cards */}
          <div className="md:hidden space-y-3">
            {monthlyData.records.map((record) => {
              const day = new Date(record.workDate).getDate();
              return (
                <div key={record.id} className="card-pop !p-4">
                  <div className="flex justify-between items-center mb-2">
                    <span className="font-bold text-primary">
                      {day}({getDayOfWeek(record.workDate)})
                    </span>
                    <StatusBadge status={record.status} />
                  </div>
                  <div className="grid grid-cols-3 gap-2 text-center text-sm">
                    <div>
                      <p className="text-xs text-gray-400">出勤</p>
                      <p className="font-mono font-bold">{formatTime(record.clockIn)}</p>
                    </div>
                    <div>
                      <p className="text-xs text-gray-400">退勤</p>
                      <p className="font-mono font-bold">{formatTime(record.clockOut)}</p>
                    </div>
                    <div>
                      <p className="text-xs text-gray-400">実働</p>
                      <p className="font-bold text-primary">{formatMinutes(record.totalWorkingMinutes)}</p>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </>
      ) : (
        <div className="text-center text-gray-400 py-8">この月の記録はありません</div>
      )}

      {submitMessage && (
        <div className="text-center text-sm font-medium text-green-600 bg-green-50 rounded-xl p-3">
          {submitMessage}
        </div>
      )}

      <div className="text-center">
        <button onClick={handleSubmit} className="btn-primary text-lg px-10">
          &#x1F4E8; 月次提出
        </button>
      </div>
    </div>
  );
}
