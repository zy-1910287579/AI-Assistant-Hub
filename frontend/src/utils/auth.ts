import { reactive } from 'vue'

const ACCESS_TOKEN_KEY = 'ai-hub-access-token'
const REFRESH_TOKEN_KEY = 'ai-hub-refresh-token'
const AUTH_PROFILE_KEY = 'ai-hub-auth-profile'

export type UserRole = 'admin' | 'user'

export type AuthProfile = {
  userId: string
  username: string
  displayName: string
  role: UserRole
}

type AuthSessionInput = {
  accessToken: string
  refreshToken?: string
  profile?: Partial<AuthProfile> | null
}

const readJson = <T>(key: string, fallback: T): T => {
  try {
    const rawValue = sessionStorage.getItem(key)
    return rawValue ? (JSON.parse(rawValue) as T) : fallback
  } catch {
    return fallback
  }
}

const normalizeRole = (value: unknown): UserRole => {
  const role = String(value || '').toLowerCase()
  return role === 'admin' ? 'admin' : 'user'
}

const decodeJwtPayload = (token?: string) => {
  if (!token) {
    return {}
  }

  try {
    const [, payload = ''] = token.split('.')
    const normalized = payload.replace(/-/g, '+').replace(/_/g, '/')
    const decoded = atob(normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '='))
    return JSON.parse(decoded) as Record<string, unknown>
  } catch {
    return {}
  }
}

const buildProfile = (input: AuthSessionInput): AuthProfile => {
  const tokenPayload = decodeJwtPayload(input.accessToken)
  const source = input.profile || {}

  return {
    userId: String(
      source.userId ||
      tokenPayload.userId ||
      tokenPayload.uid ||
      tokenPayload.sub ||
      tokenPayload.username ||
      'guest-user'
    ),
    username: String(
      source.username ||
      tokenPayload.username ||
      tokenPayload.preferred_username ||
      tokenPayload.sub ||
      'guest'
    ),
    displayName: String(
      source.displayName ||
      source.username ||
      tokenPayload.nickname ||
      tokenPayload.name ||
      tokenPayload.preferred_username ||
      '访客'
    ),
    role: normalizeRole(source.role || tokenPayload.role || tokenPayload.authority),
  }
}

const state = reactive({
  accessToken: sessionStorage.getItem(ACCESS_TOKEN_KEY) || '',
  refreshToken: sessionStorage.getItem(REFRESH_TOKEN_KEY) || '',
  profile: readJson<AuthProfile | null>(AUTH_PROFILE_KEY, null),
})

if (state.accessToken && !state.profile) {
  state.profile = buildProfile({ accessToken: state.accessToken })
}

export const authState = state

export const setAuthSession = (input: AuthSessionInput) => {
  const profile = buildProfile(input)

  state.accessToken = input.accessToken
  state.refreshToken = input.refreshToken || ''
  state.profile = profile

  sessionStorage.setItem(ACCESS_TOKEN_KEY, state.accessToken)
  sessionStorage.setItem(REFRESH_TOKEN_KEY, state.refreshToken)
  sessionStorage.setItem(AUTH_PROFILE_KEY, JSON.stringify(profile))

  return profile
}

export const clearAuthSession = () => {
  state.accessToken = ''
  state.refreshToken = ''
  state.profile = null

  sessionStorage.removeItem(ACCESS_TOKEN_KEY)
  sessionStorage.removeItem(REFRESH_TOKEN_KEY)
  sessionStorage.removeItem(AUTH_PROFILE_KEY)
}

export const getAccessToken = () => state.accessToken

export const getRefreshToken = () => state.refreshToken

export const getStoredAuthProfile = () => state.profile

export const getAuthUserId = () => state.profile?.userId || ''

export const getAuthRole = (): UserRole | '' => state.profile?.role || ''

export const isAuthenticated = () => Boolean(state.accessToken && state.profile)

export const getDefaultRouteByRole = (role = getAuthRole()) =>
  role === 'admin' ? '/admin/home' : '/user/home'
