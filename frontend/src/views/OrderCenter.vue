<template>
  <div class="order-center" :class="{ dark: isDark }">
    <div class="container">
      <div class="hero">
        <div>
          <p class="eyebrow">User Portal</p>
          <h1>订单服务中心</h1>
        </div>
      </div>

      <section class="panel">
        <div class="panel-header">
          <h2>我的订单</h2>
          <button class="query-btn" @click="loadOrders">刷新</button>
        </div>

        <div class="status-tabs">
          <button
            v-for="tab in orderTabs"
            :key="tab.value"
            :class="{ active: currentStatus === tab.value }"
            @click="applyStatus(tab.value)"
          >
            {{ tab.label }}
          </button>
        </div>

        <div v-if="!orders.length" class="pending-tip">暂无订单数据</div>
        <div v-else class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>订单号</th>
                <th>商品</th>
                <th>数量</th>
                <th>金额</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="order in orders" :key="order.orderId">
                <td>{{ order.orderId }}</td>
                <td>{{ order.productName }}</td>
                <td>{{ order.quantity }}</td>
                <td>{{ order.totalAmount }}</td>
                <td>{{ orderStatusText(order.status) }}</td>
                <td>
                  <button class="tiny-btn" @click="showDetail(order.orderId)">
                    详情
                  </button>
                  <button
                    class="tiny-btn danger"
                    :disabled="Number(order.status) !== 0"
                    @click="cancel(order.orderId)"
                  >
                    取消
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
      <section v-if="detailOrder" class="panel detail-grid">
        <h3>订单详情</h3>
        <div class="kv">
          <span>订单号</span><strong>{{ detailOrder.orderId }}</strong>
        </div>
        <div class="kv">
          <span>商品</span><strong>{{ detailOrder.productName || "-" }}</strong>
        </div>
        <div class="kv">
          <span>状态</span
          ><strong>{{ orderStatusText(detailOrder.status) }}</strong>
        </div>
        <div class="kv">
          <span>数量</span><strong>{{ detailOrder.quantity || 0 }}</strong>
        </div>
        <div class="kv">
          <span>金额</span><strong>{{ detailOrder.totalAmount || 0 }}</strong>
        </div>
        <div class="kv">
          <span>物流公司</span
          ><strong>{{ detailOrder.logisticsCompany || "-" }}</strong>
        </div>
        <div class="kv">
          <span>物流单号</span
          ><strong>{{ detailOrder.trackingNumber || "-" }}</strong>
        </div>
        <div class="kv">
          <span>物流信息</span
          ><strong>{{ detailOrder.logisticsInfo || "-" }}</strong>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useDark } from "@vueuse/core";
import { orderAPI, userAPI } from "../services/api";
import { orderStatusText } from "../utils/display";

const isDark = useDark();
const orders = ref([]);
const detailOrder = ref(null);
const currentStatus = ref("all");
const orderTabs = [
  { value: "all", label: "全部" },
  { value: 0, label: "待付款" },
  { value: 1, label: "待发货" },
  { value: 2, label: "已发货" },
  { value: 3, label: "已签收" },
  { value: 4, label: "交易成功" },
  { value: -1, label: "已取消" },
];

const loadOrders = async () => {
  detailOrder.value = null;
  if (currentStatus.value === "all") {
    orders.value = await userAPI.getMyOrders();
    return;
  }
  orders.value = await orderAPI.getOrdersByStatus(currentStatus.value);
};
const applyStatus = async (status) => {
  currentStatus.value = status;
  await loadOrders();
};
const showDetail = async (orderId) => {
  detailOrder.value = await orderAPI.getOrderDetail(orderId);
};
const cancel = async (orderId) => {
  await orderAPI.cancelOrder(orderId);
  await loadOrders();
};

onMounted(loadOrders);
</script>

<style scoped lang="scss">
.order-center {
  min-height: calc(100vh - 64px);
  padding: 2rem;
  background: var(--bg-color);
}

.container {
  max-width: 1280px;
  margin: 0 auto;
}

.hero {
  padding: 2rem;
  border-radius: 1.5rem;
  background: linear-gradient(
    135deg,
    rgba(37, 99, 235, 0.14),
    rgba(16, 185, 129, 0.1)
  );

  .eyebrow {
    color: #2563eb;
    font-size: 0.85rem;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 0.08em;
  }

  h1 {
    margin: 0.5rem 0 0.85rem;
    font-size: 2.1rem;
  }

  p {
    color: #64748b;
  }
}

.panel {
  margin-top: 1.5rem;
  padding: 1.5rem;
  border-radius: 1.4rem;
  background: rgba(255, 255, 255, 0.92);
}

// 重构的 panel-header 样式
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;

  h2 {
    margin: 0;
    font-size: 1.5rem;
    font-weight: 600;
    color: #1e293b;
  }
}

// 重构的 query-btn 样式
.query-btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 0.75rem;
  background: #2563eb;
  color: white;
  font-weight: 600;
  font-size: 0.875rem;
  cursor: pointer;
  transition: background 0.2s;
  box-shadow: 0 2px 4px rgba(37, 99, 235, 0.2);

  &:hover {
    background: #1d4ed8;
  }
}

// 优化的 status-tabs 样式
.status-tabs {
  margin-top: 0.75rem;
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  align-items: center;

  button {
    background: #dbeafe;
    color: #1e3a8a;
    border: none;
    border-radius: 0.75rem;
    padding: 0.5rem 1rem;
    font-size: 0.9rem;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover {
      background: #bfdbfe;
    }

    &.active {
      background: #2563eb;
      color: #fff;
      box-shadow: 0 2px 4px rgba(37, 99, 235, 0.2);
    }
  }
}

.tiny-btn {
  margin-right: 6px;
  background: #2563eb;
  color: #fff;
  border: 0;
  border-radius: 7px;
  padding: 4px 8px;
  cursor: pointer;
}
.tiny-btn.danger {
  background: #dc2626;
}
.tiny-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}
.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}
.kv {
  background: #f8fafc;
  border-radius: 10px;
  padding: 10px;
  display: grid;
  gap: 4px;
}
.kv span {
  color: #64748b;
  font-size: 13px;
}

.form-grid {
  margin-top: 1.5rem;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
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

.placeholder-card {
  margin-top: 1.25rem;
  padding: 1.25rem;
  border-radius: 1rem;
  background: rgba(248, 250, 252, 0.9);

  h3 {
    margin-bottom: 0.8rem;
  }

  ul {
    padding-left: 1.2rem;
    display: grid;
    gap: 0.45rem;
  }
}

.table-wrap {
  margin-top: 1rem;
  overflow: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  border: 1px solid rgba(148, 163, 184, 0.28);
  padding: 0.65rem 0.8rem;
  text-align: left;
}

.pending-tip {
  margin-top: 1rem;
  color: #64748b;
}

.dark {
  .hero p,
  .panel-header p,
  .pending-tip,
  .placeholder-card li {
    color: #cbd5e1;
  }

  .panel,
  .placeholder-card,
  .form-grid input {
    background: rgba(15, 23, 42, 0.92);
  }

  .form-grid input {
    border-color: rgba(148, 163, 184, 0.18);
    color: #e2e8f0;
  }

  th,
  td {
    color: #e2e8f0;
  }

  .kv span {
    color: #94a3b8;
  }

  // 深色模式下的按钮样式
  .status-tabs button {
    background: #334155;
    color: #cbd5e1;

    &:hover {
      background: #475569;
    }

    &.active {
      background: #3b82f6;
      color: #fff;
    }
  }

  .query-btn {
    background: #3b82f6;

    &:hover {
      background: #2563eb;
    }
  }

  .tiny-btn {
    background: #3b82f6;
  }
  .tiny-btn.danger {
    background: #ef4444;
  }
}

@media (max-width: 768px) {
  .order-center {
    padding: 1rem;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  // 移动端适配
  .panel-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;

    .query-btn {
      align-self: flex-end; // 按钮靠右
    }
  }
}
</style>
