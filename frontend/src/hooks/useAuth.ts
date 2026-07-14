"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { isAuthenticated, getStoredUser, clearAuth } from "@/lib/auth";
import { withBasePath } from "@/lib/api-client";

export function useAuth() {
  const router = useRouter();
  const [user, setUser] = useState<{ name: string; role: string } | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isAuthenticated()) {
      router.push(withBasePath("/login"));
      return;
    }
    setUser(getStoredUser());
    setLoading(false);
  }, [router]);

  const logout = () => {
    clearAuth();
    router.push(withBasePath("/login"));
  };

  return { user, loading, logout };
}
