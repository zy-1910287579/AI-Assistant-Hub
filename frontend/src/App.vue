<script setup lang="ts">
import { computed } from "vue";
import { RouterLink, RouterView, useRoute, useRouter } from "vue-router";
import { useDark, useToggle } from "@vueuse/core";
import {
  ArrowRightStartOnRectangleIcon,
  MoonIcon,
  SunIcon,
} from "@heroicons/vue/24/outline";
import { authAPI } from "./services/api";
import { authState } from "./utils/auth";

const route = useRoute();
const router = useRouter();
const isDark = useDark();
const toggleDark = useToggle(isDark);

const navMap = {
  admin: [
    { label: "管理台", to: "/admin/home" },
    { label: "用户管理", to: "/admin/users" },
    { label: "工单管理", to: "/admin/tickets" },
    { label: "智能助手", to: "/admin/assistant" },
    { label: "文档上传", to: "/admin/knowledge-base" },
    { label: "模型调试", to: "/admin/model-playground" }
  ],
  user: [
    { label: "用户首页", to: "/user/home" },
    { label: "个人中心", to: "/user/profile" },
    { label: "智能客服", to: "/user/customer-service" },
    { label: "订单服务", to: "/user/orders" },
    { label: "我的工单", to: "/user/tickets" },
    { label: "娱乐模块", to: "/user/entertainment/game" },
  ],
} as const;

const showShell = computed(() => route.name !== "login" && !!authState.profile);
const navItems = computed(() => navMap[authState.profile?.role || "user"]);
const roleLabel = computed(() =>
  authState.profile?.role === "admin" ? "管理端" : "用户端",
);

const handleLogout = async () => {
  await authAPI.logout();
  await router.replace("/login");
};
</script>

<template>
  <div class="app" :class="{ dark: isDark }">
    <nav v-if="showShell" class="navbar">
      <div class="brand-section">
        <router-link to="/" class="logo">XiaoMai Service Hub</router-link>
        <div class="nav-links">
          <router-link
            v-for="item in navItems"
            :key="item.to"
            :to="item.to"
            class="nav-link"
          >
            {{ item.label }}
          </router-link>
        </div>
      </div>
      <div class="nav-actions">
        <div class="identity-card">
          <span class="role-tag">{{ roleLabel }}</span>
          <div>
            <strong>{{
              authState.profile?.displayName || authState.profile?.username
            }}</strong>
            <small>ID: {{ authState.profile?.userId }}</small>
          </div>
        </div>
        <button @click="toggleDark()" class="theme-toggle">
          <SunIcon v-if="isDark" class="icon" />
          <MoonIcon v-else class="icon" />
        </button>
        <button class="logout-btn" @click="handleLogout">
          <ArrowRightStartOnRectangleIcon class="icon" />
        </button>
      </div>
    </nav>
    <router-view v-slot="{ Component }">
      <transition name="fade" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
  </div>
</template>

<style lang="scss">
:root {
  --bg-color: #f5f5f5;
  --text-color: #333;
}

.dark {
  --bg-color: #1a1a1a;
  --text-color: #fff;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html,
body {
  height: 100%;
}

body {
  font-family:
    -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Oxygen, Ubuntu,
    Cantarell, "Open Sans", "Helvetica Neue", sans-serif;
  color: var(--text-color);
  background: var(--bg-color);
  min-height: 100vh;
}

.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  min-height: 64px;
  padding: 0.75rem 1.5rem;
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(16px);
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid rgba(148, 163, 184, 0.12);

  .brand-section {
    min-width: 0;
    display: flex;
    align-items: center;
    gap: 1.5rem;
  }

  .logo {
    flex-shrink: 0;
    font-size: 1.35rem;
    font-weight: bold;
    text-decoration: none;
    color: inherit;
    background: linear-gradient(45deg, #007cf0, #00dfd8);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }

  .nav-links {
    display: flex;
    align-items: center;
    gap: 0.4rem;
    flex-wrap: wrap;
  }

  .nav-link {
    padding: 0.55rem 0.8rem;
    border-radius: 999px;
    color: inherit;
    text-decoration: none;
    font-size: 0.9rem;
  }

  .nav-link.router-link-active {
    background: rgba(37, 99, 235, 0.12);
    color: #2563eb;
  }

  .nav-actions {
    display: flex;
    align-items: center;
    gap: 0.75rem;
  }

  .identity-card {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 0.5rem 0.85rem;
    border-radius: 999px;
    background: rgba(255, 255, 255, 0.45);

    strong,
    small {
      display: block;
    }

    small {
      font-size: 0.875rem;
      opacity: 0.68;
    }
  }

  .role-tag {
    padding: 0.3rem 0.6rem;
    border-radius: 999px;
    background: rgba(37, 99, 235, 0.12);
    color: #2563eb;
    font-size: 0.8rem;
    font-weight: 700;
  }

  .theme-toggle,
  .logout-btn {
    width: 42px;
    height: 42px;
    display: grid;
    place-items: center;
    background: none;
    border: none;
    cursor: pointer;
    border-radius: 50%;
    transition: background-color 0.3s;

    &:hover {
      background: rgba(15, 23, 42, 0.08);
    }

    .icon {
      width: 1.2rem;
      height: 1.2rem;
      color: var(--text-color);
    }
  }

  .dark & {
    background: rgba(2, 6, 23, 0.8);
    border-bottom: 1px solid rgba(255, 255, 255, 0.06);

    .identity-card {
      background: rgba(15, 23, 42, 0.85);
    }

    .nav-link.router-link-active,
    .role-tag {
      background: rgba(96, 165, 250, 0.16);
      color: #93c5fd;
    }

    .theme-toggle:hover,
    .logout-btn:hover {
      background: rgba(255, 255, 255, 0.08);
    }
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .navbar {
    padding: 1rem;
    align-items: flex-start;
    flex-direction: column;
    gap: 0.75rem;
  }

  .brand-section,
  .nav-actions {
    width: 100%;
    justify-content: space-between;
  }

  .brand-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }

  .nav-links {
    width: 100%;
    overflow-x: auto;

    .nav-link {
      white-space: nowrap;
    }
  }

  .identity-card {
    min-width: 0;
    flex: 1;
  }
}
</style>
