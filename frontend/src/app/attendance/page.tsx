"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { isAuthenticated } from "@/lib/auth";
import { withBasePath } from "@/lib/api-client";
import StatusBadge from "@/components/StatusBadge";

interface MockRecord {
  date: string;
  dayOfWeek: string;
  clockIn: string;
  clockOut: string;
  breakMinutes: number;
  workMinutes: number;
  status: "DRAFT" | "SUBMITTED" | "APPROVED" | "REJECTED";
}

const mockData: MockRecord[] = [
  { date: "7/1", dayOfWeek: "火", clockIn: "09:00", clockOut: "18:00", breakMinutes: 60, workMinutes: 480, status: "APPROVED" },
  { date: "7/2", dayOfWeek: "水", clockIn: "09:15", clockOut: "18:30", breakMinutes: 60, workMinutes: 495, status: "APPROVED" },
  { date: "7/3", dayOfWeek: "木", clockIn: "09:00", clockOut: "17:00", breakMinutes: 45, workMinutes: 435, status: "DRAFT" },
  { date: "7/4", dayOfWeek: "金", clockIn: "10:00", clockOut: "19:00", breakMinutes: 60, workMinutes: 480, status: "DRAFT" },
  { date: "7/7", dayOfWeek: "月", clockIn: "09:30", clockOut: "18:30", breakMinutes: 60, workMinutes: 480, status: "DRAFT" },
];

export default function AttendancePage() {
  const router = useRouter();
  const [currentMonth] = useState("2026年7月");

  useEffect(() => {
    if (!isAuthenticated()) {
      router.push(withBasePath("/login"));
    }
  }, [router]);

  const formatMinutes = (minutes: number) => {
    const h = Math.floor(minutes / 60);
    const m = minutes % 60;
    return `${h}:${String(m).padStart(2, "0")}`;
  };

  const totalWork = mockData.reduce((sum, r) => sum + r.workMinutes, 0);

  return (
    <div className="space-y-6">
      {/* Month Picker */}
      <div className="flex items-center justify-center gap-4">
        <button className="text-2xl text-primary hover:scale-110 transition-transform">&#x25C0;</button>
        <h1 className="text-2xl font-extrabold bg-gradient-to-r from-primary to-accent bg-clip-text text-transparent">
          {currentMonth}
        </h1>
        <button className="text-2xl text-primary hover:scale-110 transition-transform">&#x25B6;</button>
      </div>

      {/* Summary Card */}
      <div className="card-pop">
        <div className="grid grid-cols-3 gap-4 text-center">
          <div>
            <p className="text-xs text-gray-500 font-medium">実労働</p>
            <p className="text-2xl font-extrabold text-primary">{formatMinutes(totalWork)}</p>
          </div>
          <div>
            <p className="text-xs text-gray-500 font-medium">所定</p>
            <p className="text-2xl font-extrabold text-gray-600">160:00</p>
          </div>
          <div>
            <p className="text-xs text-gray-500 font-medium">過不足</p>
            <p className="text-2xl font-extrabold text-danger">
              -{formatMinutes(9600 - totalWork)}
            </p>
          </div>
        </div>
      </div>

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
            {mockData.map((record) => (
              <tr key={record.date} className="border-b border-purple-50 hover:bg-purple-50/50 transition-colors">
                <td className="py-3 px-2 font-medium">
                  {record.date}({record.dayOfWeek})
                </td>
                <td className="py-3 px-2 text-center font-mono">{record.clockIn}</td>
                <td className="py-3 px-2 text-center font-mono">{record.clockOut}</td>
                <td className="py-3 px-2 text-center">{record.breakMinutes}分</td>
                <td className="py-3 px-2 text-center font-bold">{formatMinutes(record.workMinutes)}</td>
                <td className="py-3 px-2 text-center">
                  <StatusBadge status={record.status} />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Mobile Cards */}
      <div className="md:hidden space-y-3">
        {mockData.map((record) => (
          <div key={record.date} className="card-pop !p-4">
            <div className="flex justify-between items-center mb-2">
              <span className="font-bold text-primary">
                {record.date}({record.dayOfWeek})
              </span>
              <StatusBadge status={record.status} />
            </div>
            <div className="grid grid-cols-3 gap-2 text-center text-sm">
              <div>
                <p className="text-xs text-gray-400">出勤</p>
                <p className="font-mono font-bold">{record.clockIn}</p>
              </div>
              <div>
                <p className="text-xs text-gray-400">退勤</p>
                <p className="font-mono font-bold">{record.clockOut}</p>
              </div>
              <div>
                <p className="text-xs text-gray-400">実働</p>
                <p className="font-bold text-primary">{formatMinutes(record.workMinutes)}</p>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Submit Button */}
      <div className="text-center">
        <button className="btn-primary text-lg px-10">
          &#x1F4E8; 月次提出
        </button>
      </div>
    </div>
  );
}
