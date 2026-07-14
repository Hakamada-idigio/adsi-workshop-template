"use client";

import { useState, useCallback } from "react";
import { apiFetch } from "@/lib/api-client";
import { AttendanceDetailResponse, MonthlyAttendanceResponse, AttendanceRecordResponse } from "@/lib/types";

export function useAttendance() {
  const [dailyData, setDailyData] = useState<AttendanceDetailResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchDaily = useCallback(async (date?: string) => {
    setLoading(true);
    setError(null);
    try {
      const params = date ? `?date=${date}` : "";
      const data = await apiFetch<AttendanceDetailResponse>(`/attendance/daily${params}`);
      setDailyData(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "取得に失敗しました");
    } finally {
      setLoading(false);
    }
  }, []);

  const clockIn = useCallback(async () => {
    await apiFetch<AttendanceRecordResponse>("/attendance/clock-in", { method: "POST" });
    await fetchDaily();
  }, [fetchDaily]);

  const clockOut = useCallback(async () => {
    await apiFetch<AttendanceRecordResponse>("/attendance/clock-out", { method: "POST" });
    await fetchDaily();
  }, [fetchDaily]);

  const breakStart = useCallback(async () => {
    await apiFetch("/attendance/break/start", { method: "POST" });
    await fetchDaily();
  }, [fetchDaily]);

  const breakEnd = useCallback(async () => {
    await apiFetch("/attendance/break/end", { method: "POST" });
    await fetchDaily();
  }, [fetchDaily]);

  const fetchMonthly = useCallback(async (yearMonth: string) => {
    const data = await apiFetch<MonthlyAttendanceResponse>(
      `/attendance/monthly?yearMonth=${yearMonth}`
    );
    return data;
  }, []);

  const submitMonthly = useCallback(async (yearMonth: string) => {
    await apiFetch("/attendance/submit", {
      method: "POST",
      body: JSON.stringify({ yearMonth }),
    });
  }, []);

  return {
    dailyData,
    loading,
    error,
    fetchDaily,
    clockIn,
    clockOut,
    breakStart,
    breakEnd,
    fetchMonthly,
    submitMonthly,
  };
}
