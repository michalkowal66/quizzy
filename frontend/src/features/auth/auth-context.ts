import { createContext } from 'react'
import type { AuthResponse, AuthUser } from './types'

export interface AuthContextValue {
  user: AuthUser | null
  isAuthenticated: boolean
  signIn: (auth: AuthResponse) => void
  signOut: () => void
}

export const AuthContext = createContext<AuthContextValue | null>(null)
