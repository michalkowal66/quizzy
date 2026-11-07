import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'

import { setToken, setUnauthorizedHandler } from '@/lib/api'
import { AuthContext } from './auth-context'
import type { AuthResponse, AuthUser } from './types'

const USER_KEY = 'quizzy.user'

function readStoredUser(): AuthUser | null {
  try {
    const raw = localStorage.getItem(USER_KEY)
    return raw ? (JSON.parse(raw) as AuthUser) : null
  } catch {
    return null
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(readStoredUser)

  const signIn = useCallback((auth: AuthResponse) => {
    const { token, ...profile } = auth
    setToken(token)
    localStorage.setItem(USER_KEY, JSON.stringify(profile))
    setUser(profile)
  }, [])

  const signOut = useCallback(() => {
    setToken(null)
    localStorage.removeItem(USER_KEY)
    setUser(null)
  }, [])

  useEffect(() => {
    // Sign out automatically when the API reports an expired or invalid token.
    setUnauthorizedHandler(signOut)
    return () => setUnauthorizedHandler(null)
  }, [signOut])

  const value = useMemo(
    () => ({ user, isAuthenticated: user !== null, signIn, signOut }),
    [user, signIn, signOut],
  )

  return <AuthContext value={value}>{children}</AuthContext>
}
