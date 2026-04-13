<template>
  <div class="register-page">
    <div class="card">
      <h1>用户注册</h1>
      <p class="subtitle">创建新账号后，可直接登录用户端。</p>

      <form class="form" @submit.prevent="submit">
        <input v-model.trim="form.username" placeholder="用户名" maxlength="40" />
        <input v-model="form.password" type="password" placeholder="密码" maxlength="40" />
        <input v-model.trim="form.phoneNumber" placeholder="手机号（可选）" maxlength="20" />
        <input v-model.trim="form.email" placeholder="邮箱（可选）" maxlength="100" />
        <p v-if="error" class="error">{{ error }}</p>
        <p v-if="success" class="success">{{ success }}</p>
        <button :disabled="submitting">{{ submitting ? "注册中..." : "注册" }}</button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { authAPI } from "../services/api";

const router = useRouter();
const submitting = ref(false);
const error = ref("");
const success = ref("");
const form = reactive({
  username: "",
  password: "",
  phoneNumber: "",
  email: "",
});

const submit = async () => {
  if (!form.username || !form.password) {
    error.value = "用户名和密码为必填项";
    return;
  }

  submitting.value = true;
  error.value = "";
  success.value = "";
  try {
    await authAPI.register({
      username: form.username,
      password: form.password,
      phoneNumber: form.phoneNumber,
      email: form.email,
      role: "user",
    });
    success.value = "注册成功，正在跳转登录页...";
    setTimeout(() => router.replace("/login"), 800);
  } catch (e) {
    error.value = e?.message || "注册失败";
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.register-page { min-height: 100vh; display: grid; place-items: center; padding: 24px; color: #1e293b; }
.card { width: min(460px, 100%); padding: 24px; border-radius: 16px; background: rgba(255,255,255,.95); color: #0f172a; }
.card h1 { color: #1e293b; }
.subtitle { color: #475569; margin: 8px 0 16px; }
.form { display: grid; gap: 12px; }
input { padding: 10px 12px; border: 1px solid #cbd5e1; border-radius: 10px; font: inherit; color: #0f172a; background: #ffffff; }
button { padding: 10px 12px; border: 0; border-radius: 10px; background: #2563eb; color: #fff; cursor: pointer; font-weight: 600; }
button:hover { background: #1d4ed8; }
button:disabled { opacity: 0.6; cursor: not-allowed; }
.error { color: #dc2626; font-weight: 600; }
.success { color: #16a34a; font-weight: 600; }
</style>
