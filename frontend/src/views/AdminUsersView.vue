<template>
  <div class="page">
    <div class="toolbar">
      <h2>用户管理</h2>
      <button @click="load">刷新用户</button>
    </div>
    <p v-if="notice" class="notice">{{ notice }}</p>
    <div class="filters">
      <input v-model.trim="keyword" placeholder="按用户名模糊查询" />
      <select v-model="statusFilter">
        <option value="all">全部状态</option>
        <option value="1">启用</option>
        <option value="0">禁用</option>
      </select>
      <select v-model="vipFilter">
        <option value="all">全部VIP</option>
        <option v-for="level in vipLevels" :key="level" :value="String(level)">
          {{ vipLevelText(level) }}
        </option>
      </select>
    </div>
    <table>
      <thead>
        <tr>
          <th>用户ID</th><th>用户名</th><th>角色</th><th>状态</th><th>VIP</th><th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="user in filteredUsers" :key="user.userId">
          <td>{{ user.userId }}</td>
          <td>{{ user.username }}</td>
          <td>{{ user.role }}</td>
          <td>{{ userStatusText(user.status) }}</td>
          <td>{{ vipLevelText(user.vipLevel) }}</td>
          <td class="actions">
            <button @click="toggleStatus(user)">{{ user.status === 1 ? "禁用" : "启用" }}</button>
            <button @click="raiseVip(user)">VIP+1</button>
            <button @click="openOrders(user)">详情</button>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-if="selectedUser" class="drawer-mask" @click.self="closeOrders">
      <div class="drawer">
        <div class="drawer-header">
          <h3>用户订单详情 - {{ selectedUser.username }}</h3>
          <button @click="closeOrders">关闭</button>
        </div>
        <table>
          <thead><tr><th>订单号</th><th>商品</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="order in userOrders" :key="order.orderId">
              <td>{{ order.orderId }}</td>
              <td>{{ order.productName }}</td>
              <td>{{ orderStatusText(order.status) }}</td>
              <td>
                <select :value="String(order.status)" @change="changeOrderStatus(order.orderId, Number($event.target.value))">
                  <option value="-1">已取消</option>
                  <option value="0">待付款</option>
                  <option value="1">待发货</option>
                  <option value="2">已发货</option>
                  <option value="3">已签收</option>
                  <option value="4">交易成功</option>
                </select>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { adminAPI } from "../services/api";
import { orderStatusText, userStatusText, vipLevelText } from "../utils/display";

const users = ref([]);
const keyword = ref("");
const statusFilter = ref("all");
const vipFilter = ref("all");
const selectedUser = ref(null);
const userOrders = ref([]);
const vipLevels = [0, 1, 2, 3, 4, 5];
const notice = ref("");

const load = async () => {
  users.value = await adminAPI.getAllUsers();
};
const filteredUsers = computed(() =>
  users.value.filter((user) => {
    const keywordOk = !keyword.value || String(user.username || "").includes(keyword.value);
    const statusOk = statusFilter.value === "all" || String(user.status) === statusFilter.value;
    const vipOk = vipFilter.value === "all" || String(user.vipLevel) === vipFilter.value;
    return keywordOk && statusOk && vipOk;
  }),
);

const toggleStatus = async (user) => {
  const next = Number(user.status) === 1 ? 0 : 1;
  await adminAPI.updateUserStatus(user.userId, next);
  await load();
};

const raiseVip = async (user) => {
  await adminAPI.updateVipLevel(user.userId, Number(user.vipLevel || 0) + 1);
  await load();
};

const openOrders = async (user) => {
  selectedUser.value = user;
  try {
    userOrders.value = await adminAPI.getUserOrders(user.userId);
  } catch (e) {
    notice.value = e?.message || "获取用户订单失败";
  }
};
const closeOrders = () => {
  selectedUser.value = null;
  userOrders.value = [];
};
const changeOrderStatus = async (orderId, status) => {
  try {
    await adminAPI.updateOrderStatus(orderId, status);
    notice.value = "订单状态更新成功";
    if (selectedUser.value) {
      userOrders.value = await adminAPI.getUserOrders(selectedUser.value.userId);
    }
  } catch (e) {
    notice.value = e?.message || "订单状态更新失败";
  }
};

onMounted(load);
</script>

<style scoped>
.page { padding: 24px; color: #1e293b; }
.toolbar { display: flex; justify-content: space-between; margin-bottom: 12px; }
.notice { margin-bottom: 10px; color: #1d4ed8; font-weight: 600; }
.filters { display: flex; gap: 10px; margin-bottom: 12px; }
input,select { padding: 7px 10px; border: 1px solid #cbd5e1; border-radius: 8px; color: #0f172a; background: #ffffff; }
table { width: 100%; border-collapse: collapse; background: #fff; color: #0f172a; }
th,td { border: 1px solid #e2e8f0; padding: 8px; text-align: left; color: #0f172a; }
th { background: #f1f5f9; color: #1e293b; font-weight: 700; }
.actions { display: flex; gap: 8px; }
button { padding: 6px 10px; border: 0; border-radius: 8px; background: #2563eb; color: #fff; cursor: pointer; font-weight: 600; }
button:hover { background: #1d4ed8; }
.drawer-mask { position: fixed; inset: 0; background: rgba(2, 6, 23, .45); display: grid; place-items: center; z-index: 30; }
.drawer { width: min(1100px, 95vw); max-height: 85vh; overflow: auto; background: #fff; border-radius: 14px; padding: 16px; color: #0f172a; }
.drawer-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.drawer-header h3 { color: #1e293b; }

.dark {
  background: linear-gradient(135deg, #020617 0%, #0f172a 100%);
  color: #f1f5f9;

  .notice {
    color: #60a5fa;
  }

  input, select {
    border-color: rgba(148, 163, 184, 0.2);
    background: #1e293b;
    color: #f1f5f9;

    &:focus {
      border-color: #38bdf8;
    }
  }

  table {
    background: #1e293b;
    color: #f1f5f9;

    th, td {
      border-color: rgba(148, 163, 184, 0.2);
      color: #e2e8f0;
    }

    th {
      background: #334155;
      color: #f1f5f9;
    }
  }

  button {
    background: #3b82f6;
    color: white;

    &:hover {
      background: #2563eb;
    }
  }

  .drawer-mask {
    background: rgba(0, 0, 0, 0.6);
  }

  .drawer {
    background: #1e293b;
    color: #f1f5f9;

    .drawer-header {
      h3 {
        color: #f1f5f9;
      }
    }
  }
}
</style>
