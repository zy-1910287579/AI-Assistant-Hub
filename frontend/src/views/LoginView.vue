<template>
  <div class="login-view" :class="{ dark: isDark }">
    <div class="login-card">
      <div class="brand">
        <div>
          <p class="eyebrow">Enterprise AI Service</p>
          <h1>AI智能客服平台</h1>
          <p class="subtitle">
            支持管理员运营知识库、用户侧智能客服、订单查询与娱乐模块。
          </p>
        </div>
        <button class="theme-toggle" @click="toggleDark()">
          <SunIcon v-if="isDark" class="icon" />
          <MoonIcon v-else class="icon" />
        </button>
      </div>

      <div class="role-switch">
        <button
          v-for="option in roleOptions"
          :key="option.value"
          class="role-btn"
          :class="{ active: role === option.value }"
          @click="role = option.value"
        >
          <span>{{ option.label }}</span>
          <small>{{ option.description }}</small>
        </button>
      </div>

      <form class="login-form" autocomplete="off" @submit.prevent="handleSubmit">
        <label>
          <span>账号</span>
          <input
            v-model="account"
            type="text"
            maxlength="40"
            placeholder="请输入登录账号"
          />
        </label>
        <label>
          <span>密码</span>
          <input
            v-model="password"
            type="password"
            maxlength="40"
            placeholder="请输入登录密码"
          />
        </label>
        <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>
        <button class="submit-btn" :disabled="isSubmitting">
          {{
            isSubmitting
              ? "登录中..."
              : `登录${role === "admin" ? "管理端" : "用户端"}`
          }}
        </button>
        <button type="button" class="register-btn" @click="router.push('/register')">
          没有账号？去注册
        </button>
      </form>

      <div class="login-tips">
        <div>
          <h3>管理端</h3>
          <p>维护知识库文档、验证 RAG 检索效果、进行模型能力测试。</p>
        </div>
        <div>
          <h3>用户端</h3>
          <p>进行智能客服咨询、查询订单、体验娱乐模块。</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useDark, useToggle } from "@vueuse/core";
import { MoonIcon, SunIcon } from "@heroicons/vue/24/outline";
import { authAPI } from "../services/api";
import type { UserRole } from "../utils/auth";
import { getDefaultRouteByRole } from "../utils/auth";
import { onMounted } from "vue";

const router = useRouter();
const isDark = useDark();
const toggleDark = useToggle(isDark);

const roleOptions = [
  {
    value: "user",
    label: "用户端",
    description: "客服咨询、订单与娱乐",
  },
  {
    value: "admin",
    label: "管理端",
    description: "知识库与运营管理",
  },
] as const;

const role = ref<UserRole>("user");
const account = ref("");
const password = ref("");
const isSubmitting = ref(false);
const errorMessage = ref("");

const handleSubmit = async () => {
  if (!account.value.trim() || !password.value.trim()) {
    errorMessage.value = "请输入完整的账号和密码。";
    return;
  }

  isSubmitting.value = true;
  errorMessage.value = "";

  try {
    await authAPI.login({
      account: account.value.trim(),
      password: password.value,
      role: role.value,
    });
    await router.replace(getDefaultRouteByRole(role.value));
  } catch (error: any) {
    errorMessage.value = error?.message || "登录失败，请稍后重试。";
  } finally {
    isSubmitting.value = false;
  }
};

onMounted(() => {
  account.value = "";
  password.value = "";
  errorMessage.value = "";
});
</script>

<style scoped lang="scss">
.login-view {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  background:
    radial-gradient(
      circle at 20% 30%,
      rgba(59, 130, 246, 0.15) 0%,
      transparent 50%
    ),
    radial-gradient(
      circle at 80% 70%,
      rgba(147, 51, 234, 0.15) 0%,
      transparent 50%
    ),
    radial-gradient(
      circle at 50% 50%,
      rgba(14, 165, 233, 0.1) 0%,
      transparent 70%
    ),
    var(--bg-color);
}

.login-card {
  width: min(920px, 100%);
  padding: 2rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  border-radius: 1.5rem;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(16px);
  box-shadow: 0 24px 80px rgba(15, 23, 42, 0.12);
}

.brand {
  display: flex;
  justify-content: space-between;
  gap: 1rem;

  h1 {
    font-size: 2rem;
    margin: 0.35rem 0 0.5rem;
  }
}

.eyebrow {
  color: #2563eb;
  font-size: 0.875rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.subtitle {
  color: #64748b;
}

.theme-toggle {
  width: 44px;
  height: 44px;
  display: grid;
  place-items: center;
  border: none;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.08);
  color: inherit;
  cursor: pointer;
}

.icon {
  width: 1.2rem;
  height: 1.2rem;
}

.role-switch {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1rem;
}

.role-btn {
  padding: 1rem 1.1rem;
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  border: 1px solid rgba(148, 163, 184, 0.24);
  border-radius: 1rem;
  background: transparent;
  color: inherit;
  text-align: left;
  cursor: pointer;

  span {
    font-size: 1rem;
    font-weight: 600;
  }

  small {
    color: #64748b;
  }
}

.role-btn.active {
  border-color: rgba(37, 99, 235, 0.45);
  background: rgba(37, 99, 235, 0.08);
}

.login-form {
  display: grid;
  gap: 1rem;

  label {
    display: grid;
    gap: 0.5rem;
  }

  span {
    font-size: 0.9rem;
    font-weight: 600;
  }

  input {
    padding: 0.95rem 1rem;
    border: 1px solid rgba(148, 163, 184, 0.24);
    border-radius: 0.95rem;
    background: rgba(248, 250, 252, 0.95);
    color: inherit;
    outline: none;
    font: inherit;
  }
}

.submit-btn {
  padding: 0.95rem 1rem;
  border: none;
  border-radius: 1rem;
  background: linear-gradient(135deg, #2563eb, #7c3aed);
  color: white;
  font: inherit;
  font-weight: 600;
  cursor: pointer;
}

.register-btn {
  padding: 0.95rem 1rem;
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 1rem;
  background: transparent;
  color: inherit;
  font: inherit;
  cursor: pointer;
}

.submit-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.error-text {
  color: #dc2626;
  font-size: 0.875rem;
}

.login-tips {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1rem;

  div {
    padding: 1rem;
    border-radius: 1rem;
    background: rgba(248, 250, 252, 0.9);
  }

  h3 {
    margin-bottom: 0.5rem;
  }

  p {
    color: #64748b;
    line-height: 1.6;
  }
}

.dark {
  .login-card {
    background: rgba(15, 23, 42, 0.9);
    box-shadow: 0 24px 80px rgba(0, 0, 0, 0.28);
  }

  .subtitle,
  .role-btn small,
  .login-tips p {
    color: #94a3b8;
  }

  .theme-toggle,
  .role-btn.active,
  .login-tips div,
  .login-form input {
    background: rgba(30, 41, 59, 0.82);
  }

  .role-btn,
  .login-form input {
    border-color: rgba(148, 163, 184, 0.18);
  }
}

@media (max-width: 768px) {
  .login-view {
    padding: 1rem;
  }

  .login-card {
    padding: 1.25rem;
  }

  .brand,
  .role-switch,
  .login-tips {
    grid-template-columns: 1fr;
    display: grid;
  }

  .brand {
    display: flex;
  }
}
</style>
