import { createRouter, createWebHistory } from 'vue-router'
import { getAuthRole, getDefaultRouteByRole, isAuthenticated } from '../utils/auth'

const redirectToPortal = () => (isAuthenticated() ? getDefaultRouteByRole() : '/login')

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: redirectToPortal,
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/UserRegisterView.vue'),
    },
    {
      path: '/user',
      redirect: '/user/home',
    },
    {
      path: '/user/home',
      name: 'user-home',
      component: () => import('../views/Home.vue'),
      meta: {
        requiresAuth: true,
        roles: ['user'],
      },
    },
    {
      path: '/user/customer-service',
      name: 'user-customer-service',
      component: () => import('../views/CustomerService.vue'),
      meta: {
        requiresAuth: true,
        roles: ['user'],
      },
    },
    {
      path: '/user/orders',
      name: 'user-orders',
      component: () => import('../views/OrderCenter.vue'),
      meta: {
        requiresAuth: true,
        roles: ['user'],
      },
    },
    {
      path: '/user/profile',
      name: 'user-profile',
      component: () => import('../views/UserProfileView.vue'),
      meta: {
        requiresAuth: true,
        roles: ['user'],
      },
    },
    {
      path: '/user/tickets',
      name: 'user-tickets',
      component: () => import('../views/UserTicketsView.vue'),
      meta: {
        requiresAuth: true,
        roles: ['user'],
      },
    },
    {
      path: '/user/entertainment/ai-chat',
      name: 'user-ai-chat',
      component: () => import('../views/AIChat.vue'),
      meta: {
        requiresAuth: true,
        roles: ['user'],
      },
    },
    {
      path: '/user/entertainment/game',
      name: 'user-game',
      component: () => import('../views/GameChat.vue'),
      meta: {
        requiresAuth: true,
        roles: ['user'],
      },
    },
    {
      path: '/admin',
      redirect: '/admin/home',
    },
    {
      path: '/admin/home',
      name: 'admin-home',
      component: () => import('../views/AdminHome.vue'),
      meta: {
        requiresAuth: true,
        roles: ['admin'],
      },
    },
    {
      path: '/admin/users',
      name: 'admin-users',
      component: () => import('../views/AdminUsersView.vue'),
      meta: {
        requiresAuth: true,
        roles: ['admin'],
      },
    },
    {
      path: '/admin/tickets',
      name: 'admin-tickets',
      component: () => import('../views/AdminTicketsView.vue'),
      meta: {
        requiresAuth: true,
        roles: ['admin'],
      },
    },
    {
      path: '/admin/assistant',
      name: 'admin-assistant',
      component: () => import('../views/AdminAssistantView.vue'),
      meta: {
        requiresAuth: true,
        roles: ['admin'],
      },
    },
    {
      path: '/admin/knowledge-base',
      name: 'admin-knowledge-base',
      component: () => import('../views/ChatPDF.vue'),
      meta: {
        requiresAuth: true,
        roles: ['admin'],
      },
    },
    {
      path: '/admin/model-playground',
      name: 'admin-model-playground',
      component: () => import('../views/AIChat.vue'),
      meta: {
        requiresAuth: true,
        roles: ['admin'],
      },
    },
    {
      path: '/customer-service',
      redirect: '/user/customer-service',
    },
    {
      path: '/knowledge-base',
      alias: '/chat-pdf',
      redirect: '/admin/knowledge-base',
    },
    {
      path: '/game',
      redirect: '/user/entertainment/game',
    },
    {
      path: '/ai-chat',
      redirect: '/admin/model-playground',
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: redirectToPortal,
    },
  ],
})

router.beforeEach(to => {
  const requiresAuth = Boolean(to.meta.requiresAuth)

  if (!requiresAuth) {
    if (to.name === 'login' && isAuthenticated()) {
      return getDefaultRouteByRole()
    }

    return true
  }

  if (!isAuthenticated()) {
    return '/login'
  }

  const allowedRoles = Array.isArray(to.meta.roles) ? to.meta.roles : []

  if (allowedRoles.length) {
    const currentRole = getAuthRole()

    if (!allowedRoles.includes(currentRole)) {
      return getDefaultRouteByRole(currentRole || 'user')
    }
  }

  return true
})

export default router
