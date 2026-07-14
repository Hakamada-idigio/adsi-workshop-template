"use client";

import { useEffect, useState } from "react";
import { isAuthenticated, getStoredUser, clearAuth } from "@/lib/auth";
import { withBasePath } from "@/lib/api-client";

export function useAuth() {
  const [user, setUser] = useState<{ name: string; role: string } | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isAuthenticated()) {
      window.location.href = withBasePath("/login");
      return;
    }
    setUser(getStoredUser());
    setLoading(false);
  }, []);

  const logout = () => {
    clearAuth();
    window.location.href = withBasePath("/login");
  };

  return { user, loading, logout };
}
