export interface AuthUser {
  userId: string
  email: string
  displayName: string
  role: string
}

export interface AuthResponse extends AuthUser {
  token: string
}
