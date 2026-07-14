export interface LoginResponse {
  token: string;
  employeeName: string;
  role: "EMPLOYEE" | "MANAGER";
}

export type AttendanceStatus = "DRAFT" | "SUBMITTED" | "APPROVED" | "REJECTED";

export interface BreakRecordResponse {
  id: number;
  breakStart: string;
  breakEnd: string | null;
  durationMinutes: number | null;
}

export interface AttendanceRecordResponse {
  id: number;
  employeeId: number;
  workDate: string;
  clockIn: string;
  clockOut: string | null;
  status: AttendanceStatus;
  breaks: BreakRecordResponse[];
  totalWorkingMinutes: number | null;
}

export interface AttendanceDetailResponse {
  record: AttendanceRecordResponse | null;
  isOnBreak: boolean;
  canEdit: boolean;
}

export interface MonthlyAttendanceResponse {
  yearMonth: string;
  records: AttendanceRecordResponse[];
  totalWorkingMinutes: number;
  requiredMinutes: number;
  balanceMinutes: number;
  coreTimeStart: string;
  coreTimeEnd: string;
}
