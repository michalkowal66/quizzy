import { api } from '@/lib/api'
import type { AuthResponse } from './types'

export async function login(payload: { email: string; password: string }): Promise<AuthResponse> {
  const { data } = await api.post<AuthResponse>('/auth/login', payload)
  return data
}

export async function register(payload: {
  email: string
  displayName: string
  password: string
}): Promise<AuthResponse> {
  const { data } = await api.post<AuthResponse>('/auth/register', payload)
  return data
}
