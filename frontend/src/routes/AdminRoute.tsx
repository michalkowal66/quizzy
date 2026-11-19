import { Navigate, Outlet } from "react-router-dom";

import { useAuth } from "@/features/auth/use-auth";

export function AdminRoute() {
  const { user } = useAuth();
  return user?.role === "Admin" ? <Outlet /> : <Navigate to="/" replace />;
}
