<template>
  <div class="page">
    <div class="toolbar">
      <h2>我的工单</h2>
      <button @click="load">刷新</button>
    </div>
    <table>
      <thead>
        <tr>
          <th>ID</th><th>标题</th><th>分类</th><th>状态</th><th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="ticket in tickets" :key="ticket.ticketId">
          <td>{{ ticket.ticketId }}</td>
          <td>{{ ticket.title }}</td>
          <td>{{ ticket.category }}</td>
          <td>{{ ticketStatusText(ticket.status) }}</td>
          <td>
            <button :disabled="Number(ticket.status)!==0" @click="edit(ticket)">更新</button>
            <button :disabled="Number(ticket.status)!==0" @click="cancel(ticket.ticketId)">取消</button>
          </td>
        </tr>
      </tbody>
    </table>
    <div v-if="editingTicket" class="edit-card">
      <h3>更新工单 #{{ editingTicket.ticketId }}</h3>
      <input v-model.trim="editForm.category" placeholder="分类" />
      <textarea v-model.trim="editForm.description" rows="3" placeholder="描述"></textarea>
      <div class="actions"><button @click="submitEdit">提交</button><button @click="editingTicket=null">关闭</button></div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { userAPI } from "../services/api";
import { ticketStatusText } from "../utils/display";

const tickets = ref([]);
const editingTicket = ref(null);
const editForm = reactive({ category: "", description: "" });

const load = async () => {
  tickets.value = await userAPI.getMyTickets();
};

const cancel = async (ticketId) => {
  await userAPI.cancelMyTicket(ticketId);
  await load();
};
const edit = (ticket) => {
  editingTicket.value = ticket;
  editForm.category = ticket.category || "";
  editForm.description = ticket.description || "";
};
const submitEdit = async () => {
  if (!editingTicket.value) return;
  await userAPI.updateMyTicket(editingTicket.value.ticketId, {
    category: editForm.category,
    description: editForm.description,
  });
  editingTicket.value = null;
  await load();
};

onMounted(load);
</script>

<style scoped>
.page { padding: 24px; color: #1e293b; }
.toolbar { display: flex; justify-content: space-between; margin-bottom: 12px; }
table { width: 100%; border-collapse: collapse; background: #fff; border-radius: 8px; overflow: hidden; color: #0f172a; }
th,td { border: 1px solid #e2e8f0; padding: 8px; text-align: left; color: #0f172a; }
th { background: #f1f5f9; color: #1e293b; font-weight: 700; }
button { padding: 6px 10px; border: 0; border-radius: 8px; background: #2563eb; color: #fff; cursor: pointer; font-weight: 600; }
button:hover { background: #1d4ed8; }
button:disabled { opacity: .45; cursor: not-allowed; }
.edit-card { margin-top: 12px; display: grid; gap: 8px; background: #fff; border-radius: 10px; padding: 12px; color: #0f172a; }
.edit-card h3 { color: #1e293b; margin-bottom: 8px; }
input,textarea { border: 1px solid #cbd5e1; border-radius: 8px; padding: 8px 10px; font: inherit; color: #0f172a; background: #ffffff; }
.actions { display: flex; gap: 8px; }
</style>
