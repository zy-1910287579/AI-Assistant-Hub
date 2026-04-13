<template>
  <div class="page">
    <div class="toolbar">
      <h2>工单管理</h2>
      <button @click="load">刷新工单</button>
    </div>
    <div class="filters">
      <button v-for="item in statusTabs" :key="item.value" :class="{ active: currentStatus === item.value }" @click="currentStatus = item.value">
        {{ item.label }}
      </button>
    </div>
    <table>
      <thead>
        <tr>
          <th>ID</th><th>用户ID</th><th>标题</th><th>描述</th><th>状态</th><th>处理人</th><th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="ticket in filteredTickets" :key="ticket.ticketId">
          <td>{{ ticket.ticketId }}</td>
          <td>{{ ticket.userId }}</td>
          <td>{{ ticket.title }}</td>
          <td>{{ ticket.description || "-" }}</td>
          <td>{{ ticketStatusText(ticket.status) }}</td>
          <td>{{ ticket.assignedTo || "-" }}</td>
          <td class="actions">
            <button @click="setStatus(ticket, 1)">处理中</button>
            <button @click="setStatus(ticket, 2)">已解决</button>
            <button @click="setStatus(ticket, 3)">已关闭</button>
            <button @click="assign(ticket)">分配给我</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { adminAPI } from "../services/api";
import { authState } from "../utils/auth";
import { ticketStatusText } from "../utils/display";

const tickets = ref([]);
const currentStatus = ref("all");
const statusTabs = [
  { value: "all", label: "全部" },
  { value: 0, label: "待处理" },
  { value: 1, label: "处理中" },
  { value: 2, label: "已解决" },
  { value: 3, label: "已关闭" },
];

const load = async () => {
  tickets.value = await adminAPI.getAllTickets();
};
const filteredTickets = computed(() => {
  if (currentStatus.value === "all") return tickets.value;
  return tickets.value.filter((x) => Number(x.status) === Number(currentStatus.value));
});

const setStatus = async (ticket, status) => {
  await adminAPI.updateTicketStatus(ticket.ticketId, status);
  await load();
};

const assign = async (ticket) => {
  const assignee = authState.profile?.username || authState.profile?.userId || "admin";
  await adminAPI.assignTicket(ticket.ticketId, assignee);
  await load();
};

onMounted(load);
</script>

<style scoped>
.page { padding: 24px; color: #1e293b; }
.toolbar { display: flex; justify-content: space-between; margin-bottom: 12px; }
.filters { display: flex; gap: 8px; margin-bottom: 12px; }
table { width: 100%; border-collapse: collapse; background: #fff; color: #0f172a; }
th,td { border: 1px solid #e2e8f0; padding: 8px; text-align: left; color: #0f172a; }
th { background: #f1f5f9; color: #1e293b; font-weight: 700; }
.actions { display: flex; gap: 8px; flex-wrap: wrap; }
button { padding: 6px 10px; border: 0; border-radius: 8px; background: #2563eb; color: #fff; cursor: pointer; font-weight: 600; }
button:hover { background: #1d4ed8; }
.filters button { background: #dbeafe; color: #1e3a8a; font-weight: 600; }
.filters button.active { background: #2563eb; color: #fff; }
.filters button:hover:not(.active) { background: #bfdbfe; }

.dark {
  background: linear-gradient(135deg, #020617 0%, #0f172a 100%);
  color: #f1f5f9;

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

  .filters button {
    background: #1e3a5f;
    color: #93c5fd;

    &.active {
      background: #3b82f6;
      color: white;
    }

    &:hover:not(.active) {
      background: #1e40af;
    }
  }
}
</style>
