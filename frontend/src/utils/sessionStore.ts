import { getAuthUserId } from './auth'

const USER_ID_KEY = 'ai-hub-user-id'
const SESSION_MAP_KEY = 'ai-hub-session-map'
const ACTIVE_SESSION_KEY = 'ai-hub-active-session'

export const DEFAULT_USER_ID = 'user_Test'

type SessionScope = 'customer' | 'rag' | 'game' | string

type StoredSession = {
  sessionId: string
  title: string
  createdAt: string
  updatedAt: string
  pinned?: boolean
  fileName?: string
  fileType?: string
}

const SESSION_CONFIG: Record<string, { prefix: string; label: string }> = {
  customer: {
    prefix: 'chat',
    label: '咨询',
  },
  rag: {
    prefix: 'rag',
    label: '知识库',
  },
  game: {
    prefix: 'game',
    label: '游戏',
  },
}

const readJson = <T>(key: string, fallback: T): T => {
  try {
    const rawValue = localStorage.getItem(key)
    return rawValue ? (JSON.parse(rawValue) as T) : fallback
  } catch {
    return fallback
  }
}

const writeJson = (key: string, value: unknown) => {
  localStorage.setItem(key, JSON.stringify(value))
}

const sortSessions = (sessions: StoredSession[]) =>
  [...sessions].sort((left, right) => {
    if (Boolean(left.pinned) !== Boolean(right.pinned)) {
      return Number(Boolean(right.pinned)) - Number(Boolean(left.pinned))
    }

    return new Date(right.updatedAt).getTime() - new Date(left.updatedAt).getTime()
  })

const getSessionConfig = (scope: SessionScope) => SESSION_CONFIG[scope] || { prefix: scope, label: '会话' }

const getAllSessions = () => readJson<Record<string, StoredSession[]>>(SESSION_MAP_KEY, {})

const saveAllSessions = (sessionMap: Record<string, StoredSession[]>) => {
  writeJson(SESSION_MAP_KEY, sessionMap)
}

const getAllActiveSessionIds = () => readJson<Record<string, string>>(ACTIVE_SESSION_KEY, {})

const saveAllActiveSessionIds = (activeSessionMap: Record<string, string>) => {
  writeJson(ACTIVE_SESSION_KEY, activeSessionMap)
}

export const getStoredUserId = () => {
  const authUserId = getAuthUserId().trim()
  if (authUserId) {
    return authUserId
  }

  const userId = localStorage.getItem(USER_ID_KEY)?.trim()
  return userId || DEFAULT_USER_ID
}

export const setStoredUserId = (userId: string) => {
  const normalizedUserId = userId.trim() || DEFAULT_USER_ID
  localStorage.setItem(USER_ID_KEY, normalizedUserId)
  return normalizedUserId
}

export const getSessionsByScope = (scope: SessionScope) => {
  const sessionMap = getAllSessions()
  return Array.isArray(sessionMap[scope]) ? sortSessions(sessionMap[scope]) : []
}

export const setSessionsByScope = (scope: SessionScope, sessions: StoredSession[]) => {
  const sessionMap = getAllSessions()
  sessionMap[scope] = sortSessions(sessions)
  saveAllSessions(sessionMap)
}

export const getActiveSessionId = (scope: SessionScope) => {
  const activeSessionMap = getAllActiveSessionIds()
  return activeSessionMap[scope] || ''
}

export const setActiveSessionId = (scope: SessionScope, sessionId: string) => {
  const activeSessionMap = getAllActiveSessionIds()
  activeSessionMap[scope] = sessionId
  saveAllActiveSessionIds(activeSessionMap)
}

export const createSession = (scope: SessionScope, overrides: Partial<StoredSession> = {}) => {
  const sessions = getSessionsByScope(scope)
  const { prefix, label } = getSessionConfig(scope)
  const maxIndex = sessions.reduce((currentMax, session) => {
    const parts = String(session.sessionId || '').split('_')
    const currentValue = Number.parseInt(parts[1], 10)
    return Number.isFinite(currentValue) ? Math.max(currentMax, currentValue) : currentMax
  }, 0)
  const session = {
    sessionId: `${prefix}_${String(maxIndex + 1).padStart(3, '0')}`,
    title: `${label} ${String(maxIndex + 1).padStart(3, '0')}`,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    ...overrides,
  }

  setSessionsByScope(scope, [session, ...sessions])
  setActiveSessionId(scope, session.sessionId)
  return session
}

export const upsertSession = (scope: SessionScope, nextSession: Partial<StoredSession> & { sessionId: string }) => {
  const sessions = getSessionsByScope(scope)
  const existingIndex = sessions.findIndex(session => session.sessionId === nextSession.sessionId)
  const mergedSession = {
    ...sessions[existingIndex],
    ...nextSession,
    updatedAt: new Date().toISOString(),
  } as StoredSession

  if (existingIndex === -1) {
    setSessionsByScope(scope, [mergedSession, ...sessions])
  } else {
    const nextSessions = [...sessions]
    nextSessions.splice(existingIndex, 1)
    setSessionsByScope(scope, [mergedSession, ...nextSessions])
  }

  return mergedSession
}

export const removeSession = (scope: SessionScope, sessionId: string) => {
  const sessions = getSessionsByScope(scope).filter(session => session.sessionId !== sessionId)
  setSessionsByScope(scope, sessions)

  if (getActiveSessionId(scope) === sessionId) {
    setActiveSessionId(scope, sessions[0]?.sessionId || '')
  }

  return sessions
}

export const clearAllSessionStore = () => {
  localStorage.removeItem(USER_ID_KEY)
  localStorage.removeItem(SESSION_MAP_KEY)
  localStorage.removeItem(ACTIVE_SESSION_KEY)
}
