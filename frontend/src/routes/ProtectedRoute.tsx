import { Navigate, Outlet } from 'react-router-dom'

import { useAuth } from '@/features/auth/use-auth'

export function ProtectedRoute() {
  const { isAuthenticated } = useAuth()
  return isAuthenticated ? <Outlet /> : <Navigate to="/login" replace />
}
