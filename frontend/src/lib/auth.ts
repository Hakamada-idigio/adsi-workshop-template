export interface LoginResponse {
  token: string;
  employeeName: string;
  role: "EMPLOYEE" | "MANAGER";
}

export function getToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem("token");
}

export function setToken(token: string): void {
  localStorage.setItem("token", token);
}

export function removeToken(): void {
  localStorage.removeItem("token");
}

export function getStoredUser(): { name: string; role: string } | null {
  if (typeof window === "undefined") return null;
  const name = localStorage.getItem("userName");
  const role = localStorage.getItem("userRole");
  if (!name || !role) return null;
  return { name, role };
}

export function setStoredUser(name: string, role: string): void {
  localStorage.setItem("userName", name);
  localStorage.setItem("userRole", role);
}

export function clearAuth(): void {
  removeToken();
  localStorage.removeItem("userName");
  localStorage.removeItem("userRole");
}

export function isAuthenticated(): boolean {
  return !!getToken();
}
