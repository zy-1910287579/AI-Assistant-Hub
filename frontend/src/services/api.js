import { clearAuthSession, getAccessToken, setAuthSession } from '../utils/auth'
import { clearAllSessionStore } from '../utils/sessionStore'

export const BASE_URL = 'http://localhost:8080'
const API_PREFIX = '/api'

const buildUrl = (path, query = {}) => {
  const url = new URL(`${BASE_URL}${API_PREFIX}${path}`)

  Object.entries(query).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      url.searchParams.append(key, value)
    }
  })

  return url
}

const withAuthHeaders = headers => {
  const accessToken = getAccessToken()

  return {
    ...(headers || {}),
    ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
  }
}

const isTokenExpired = (token) => {
  if (!token) return true
  
  try {
    const [, payload = ''] = token.split('.')
    const normalized = payload.replace(/-/g, '+').replace(/_/g, '/')
    const decoded = atob(normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '='))
    const payloadObj = JSON.parse(decoded)
    const exp = payloadObj.exp
    
    if (!exp) return false
    
    const now = Math.floor(Date.now() / 1000)
    return now >= exp
  } catch {
    return false
  }
}

const handleAuthError = () => {
  clearAuthSession()
  clearAllSessionStore()
  
  if (window.location.pathname !== '/login') {
    window.location.href = '/login'
  }
}

const request = (path, options = {}, query) =>
  fetch(buildUrl(path, query), {
    ...options,
    headers: withAuthHeaders(options.headers),
  })

const parseErrorMessage = async response => {
  try {
    const contentType = response.headers.get('content-type') || ''

    if (contentType.includes('application/json')) {
      const result = await response.json()
      return result.message || result.data || `请求失败(${response.status})`
    }

    const text = await response.text()
    return text || `请求失败(${response.status})`
  } catch {
    return `请求失败(${response.status})`
  }
}

const ensureSuccess = async response => {
  if (!response.ok) {
    if (response.status === 401) {
      const token = getAccessToken()
      if (isTokenExpired(token)) {
        handleAuthError()
        throw new Error('登录已过期，请重新登录')
      }
    }
    throw new Error(await parseErrorMessage(response))
  }
}

const unwrapResult = async response => {
  await ensureSuccess(response)
  const result = await response.json()

  if (typeof result?.code === 'number' && result.code !== 200) {
    throw new Error(result.message || '请求失败')
  }

  return result?.data
}

const getStreamReader = async response => {
  await ensureSuccess(response)

  if (!response.body) {
    throw new Error('当前浏览器不支持流式响应')
  }

  return response.body.getReader()
}

const mapHistoryItemToMessages = historyItem => {
  const timestamp = historyItem.timestamp || new Date().toISOString()

  return [
    {
      role: 'user',
      content: historyItem.userMessage || '',
      timestamp: new Date(timestamp),
    },
    {
      role: 'assistant',
      content: historyItem.aiMessage || '',
      timestamp: new Date(timestamp),
      isMarkdown: true,
    },
  ]
}

export const chatAPI = {
  async talk(prompt) {
    const response = await request('/ai/talk', undefined, { prompt })
    await ensureSuccess(response)
    return response.text()
  },

  async streamTalk({ prompt, sessionId }) {
    const response = await request('/ai/talk', {
      method: 'GET',
    }, { prompt, sessionId })
    return getStreamReader(response)
  },

  async streamCustomerChat({ prompt, userId, sessionId }) {
    const response = await request('/ai/chat', {
      method: 'GET',
    }, { prompt, sessionId })

    return getStreamReader(response)
  },

  async streamRagChat({ prompt, userId, sessionId }) {
    const response = await request('/ai/ragchat', {
      method: 'GET',
    }, { prompt, sessionId })

    return getStreamReader(response)
  },

  async getHistoryMessages({ userId, sessionId }) {
    const response = await request('/history/getHistoryRecord', undefined, { sessionId })
    const historyItems = await unwrapResult(response)

    if (!Array.isArray(historyItems)) {
      return []
    }

    return historyItems.flatMap(mapHistoryItemToMessages)
  },

  async clearHistory({ userId, sessionId }) {
    const response = await request('/history/remove', {
      method: 'DELETE',
    }, { sessionId })

    return unwrapResult(response)
  },

  async uploadDocument({ userId, sessionId, file }) {
    const formData = new FormData()
    formData.append('file', file)

    const response = await request('/store/upload', {
      method: 'POST',
      body: formData,
    }, { sessionId })

    await unwrapResult(response)
  },

  async listDocuments({ sessionId }) {
    const response = await request('/store/list', undefined, { sessionId })
    const names = await unwrapResult(response)
    return Array.isArray(names) ? names : []
  },

  async clearSessionDocuments({ userId, sessionId }) {
    const response = await request('/store/session', {
      method: 'DELETE',
    }, { sessionId })

    return unwrapResult(response)
  },

  async downloadSessionDocument({ sessionId, fileName }) {
    const response = await request(`/store/preview/${encodeURIComponent(fileName)}`, undefined, { sessionId })

    if (response.status === 404) {
      return null
    }

    await ensureSuccess(response)

    const contentDisposition = response.headers.get('content-disposition') || ''
    const matches = contentDisposition.match(/filename\*?=(?:UTF-8'')?["']?([^"';]+)["']?/)
    const resolvedFileName = matches?.[1]
      ? decodeURIComponent(matches[1])
      : fileName || 'knowledge-document'
    const blob = await response.blob()

    return new File([blob], resolvedFileName, {
      type: blob.type || 'application/octet-stream',
    })
  },

  async previewDocument({ sessionId, fileName }) {
    return this.downloadSessionDocument({ sessionId, fileName })
  },

  async sendGameMessage({ prompt, userId, sessionId, signal }) {
    const response = await request('/ai/game', {
      method: 'GET',
      signal,
    }, { prompt, sessionId })

    return getStreamReader(response)
  },

  async streamAdminChat({ prompt, sessionId }) {
    const response = await request('/ai/adminchat', {
      method: 'GET',
    }, { prompt, sessionId })

    return getStreamReader(response)
  },
}

export const authAPI = {
  async login({ account, password, role }) {
    const response = await request('/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        account,
        password,
        role,
      }),
    })
    const data = await unwrapResult(response)
    const accessToken = data?.accessToken || data?.token || ''

    if (!accessToken) {
      throw new Error('登录接口缺少 accessToken')
    }

    return setAuthSession({
      accessToken,
      refreshToken: data?.refreshToken || '',
      profile: data?.authInfo || data?.user || {
        userId: data?.userId,
        username: data?.username || account,
        displayName: data?.displayName || data?.name || data?.authInfo?.displayName,
        role,
      },
    })
  },

  async register(payload) {
    const response = await request('/user/customer/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    })

    return unwrapResult(response)
  },

  async getProfile() {
    const response = await request('/auth/me')
    return unwrapResult(response)
  },

  async logout() {
    try {
      await request('/auth/logout', {
        method: 'POST',
      })
    } catch {
      // ignore logout request failure
    } finally {
      clearAuthSession()
      clearAllSessionStore()
    }
  },
}

export const userAPI = {
  async getProfile() {
    const response = await request('/user/customer/userInfo')
    return unwrapResult(response)
  },

  async updatePassword({ oldPassword, newPassword }) {
    const response = await request('/user/customer/password', {
      method: 'PUT',
    }, { oldPassword, newPassword })
    return Boolean(await unwrapResult(response))
  },

  async getMyOrders() {
    const response = await request('/user/order')
    const result = await unwrapResult(response)
    return Array.isArray(result) ? result : []
  },

  async getMyTickets() {
    const response = await request('/user/ticket')
    const result = await unwrapResult(response)
    return Array.isArray(result) ? result : []
  },

  async updateMyTicket(ticketId, payload) {
    const response = await request(`/user/ticket/${ticketId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    })
    return unwrapResult(response)
  },

  async cancelMyTicket(ticketId) {
    const response = await request(`/user/ticket/${ticketId}`, {
      method: 'DELETE',
    })
    return unwrapResult(response)
  },
}

export const orderAPI = {
  async getOrderDetail(orderId) {
    const response = await request(`/user/order/${orderId}`)
    return unwrapResult(response)
  },

  async getOrdersByStatus(status) {
    const response = await request(`/user/order/status/${status}`)
    const result = await unwrapResult(response)
    return Array.isArray(result) ? result : []
  },

  async createOrder(payload) {
    const response = await request('/user/order', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    })
    return unwrapResult(response)
  },

  async cancelOrder(orderId) {
    const response = await request(`/user/order/${orderId}/cancel`, {
      method: 'PUT',
    })
    return Boolean(await unwrapResult(response))
  },

  async deleteOrder(orderId) {
    const response = await request(`/user/order/${orderId}`, {
      method: 'DELETE',
    })
    return Boolean(await unwrapResult(response))
  },
}

export const adminAPI = {
  async getAllUsers() {
    const response = await request('/admin/user/userlist')
    const result = await unwrapResult(response)
    return Array.isArray(result) ? result : []
  },

  async updateUserStatus(userId, status) {
    const response = await request(`/admin/user/${userId}/status`, {
      method: 'PUT',
    }, { status })
    return Boolean(await unwrapResult(response))
  },

  async updateVipLevel(userId, vipLevel) {
    const response = await request(`/admin/user/${userId}/vip-level`, {
      method: 'PUT',
    }, { vipLevel })
    return Boolean(await unwrapResult(response))
  },

  async getUserOrders(userId) {
    const response = await request(`/admin/user/${userId}/orders`)
    const result = await unwrapResult(response)
    return Array.isArray(result) ? result : []
  },

  async getAllTickets() {
    const response = await request('/admin/ticket')
    const result = await unwrapResult(response)
    return Array.isArray(result) ? result : []
  },

  async getTicketsByStatus(status) {
    const response = await request(`/admin/ticket/status/${status}`)
    const result = await unwrapResult(response)
    return Array.isArray(result) ? result : []
  },

  async getTicketsByUserId(userId) {
    const response = await request(`/admin/ticket/user/${userId}`)
    const result = await unwrapResult(response)
    return Array.isArray(result) ? result : []
  },

  async updateTicketStatus(ticketId, status) {
    const response = await request(`/admin/ticket/${ticketId}/status`, {
      method: 'PUT',
    }, { status })
    return Boolean(await unwrapResult(response))
  },

  async assignTicket(ticketId, assignedTo) {
    const response = await request(`/admin/ticket/${ticketId}/assign`, {
      method: 'PUT',
    }, { assignedTo })
    return Boolean(await unwrapResult(response))
  },

  async updateOrderStatus(orderId, status) {
    const response = await request(`/admin/order/${orderId}/status`, {
      method: 'PUT',
    }, { status })
    return Boolean(await unwrapResult(response))
  },
}
