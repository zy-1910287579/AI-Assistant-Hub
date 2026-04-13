<template>
  <div class="page">
    <div class="hero panel">
      <div>
        <h2>{{ profile.username || "-" }}</h2>
        <p>ID: {{ profile.userId || "-" }}</p>
      </div>
      <button @click="loadProfile" :disabled="loading">{{ loading ? "加载中..." : "刷新信息" }}</button>
    </div>
    <div class="panel cards">
      <div class="info-card"><span>邮箱</span><strong>{{ profile.email || "-" }}</strong></div>
      <div class="info-card"><span>手机号</span><strong>{{ profile.phoneNumber || "-" }}</strong></div>
      <div class="info-card"><span>账号状态</span><strong>{{ userStatusText(profile.status) }}</strong></div>
      <div class="info-card"><span>VIP等级</span><strong>{{ vipLevelText(profile.vipLevel) }}</strong></div>
      <div class="info-card"><span>积分</span><strong>{{ profile.pointsBalance ?? 0 }}</strong></div>
    </div>

    <div class="panel">
      <h3>修改密码</h3>
      <div class="form">
        <input v-model="oldPassword" type="password" placeholder="旧密码" />
        <input v-model="newPassword" type="password" placeholder="新密码" />
        <button @click="changePassword" :disabled="submitting">{{ submitting ? "提交中..." : "提交" }}</button>
      </div>
      <p v-if="message">{{ message }}</p>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { userAPI } from "../services/api";
import { userStatusText, vipLevelText } from "../utils/display";

const profile = ref({});
const loading = ref(false);
const submitting = ref(false);
const oldPassword = ref("");
const newPassword = ref("");
const message = ref("");

const loadProfile = async () => {
  loading.value = true;
  try {
    profile.value = await userAPI.getProfile();
  } finally {
    loading.value = false;
  }
};

const changePassword = async () => {
  if (!oldPassword.value || !newPassword.value) return;
  submitting.value = true;
  message.value = "";
  try {
    await userAPI.updatePassword({ oldPassword: oldPassword.value, newPassword: newPassword.value });
    message.value = "密码更新成功";
    oldPassword.value = "";
    newPassword.value = "";
  } catch (e) {
    message.value = e?.message || "密码更新失败";
  } finally {
    submitting.value = false;
  }
};

onMounted(loadProfile);
</script>

<style scoped>
.page { padding: 24px; display: grid; gap: 16px; color: #1e293b; }
.panel { background: rgba(255,255,255,.95); border-radius: 12px; padding: 16px; color: #0f172a; }
.hero { display: flex; justify-content: space-between; align-items: center; }
.hero h2 { color: #1e293b; }
.hero p { color: #475569; margin-top: 4px; }
.cards { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; }
.info-card { background: #f8fafc; border-radius: 10px; padding: 12px; display: grid; gap: 6px; color: #0f172a; }
.info-card span { color: #475569; font-size: 13px; font-weight: 600; }
.info-card strong { color: #0f172a; font-weight: 700; }
.form { display: grid; gap: 10px; max-width: 360px; }
input { padding: 10px; border: 1px solid #cbd5e1; border-radius: 8px; color: #0f172a; background: #ffffff; }
button { width: fit-content; padding: 8px 12px; border: 0; border-radius: 8px; background: #2563eb; color: #fff; cursor: pointer; font-weight: 600; }
button:hover { background: #1d4ed8; }
button:disabled { opacity: 0.6; cursor: not-allowed; }
@media (max-width: 960px) { .cards { grid-template-columns: 1fr; } }
</style>
