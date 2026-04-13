<template>
  <div class="portal-home" :class="{ dark: isDark }">
    <div class="container">
      <div class="hero">
        <div>
          <p class="eyebrow">Admin Workspace</p>
          <h1>智能客服管理台</h1>
          <p>
            维护知识库、测试检索质量，并为智能客服提供稳定的售前售后知识支持。
          </p>
        </div>
      </div>

      <div class="cards-grid">
        <router-link
          v-for="item in cards"
          :key="item.title"
          :to="item.route"
          class="card"
        >
          <component :is="item.icon" class="icon" />
          <h2>{{ item.title }}</h2>
          <p>{{ item.description }}</p>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useDark } from "@vueuse/core";
import {
  CircleStackIcon,
  CpuChipIcon,
  DocumentMagnifyingGlassIcon,
  UsersIcon,
  TicketIcon,
  SparklesIcon,
} from "@heroicons/vue/24/outline";

const isDark = useDark();

const cards = ref([
  {
    title: "文档上传模块",
    description: "上传政策文档，供用户端 RAG 问答检索和引用。",
    route: "/admin/knowledge-base",
    icon: CircleStackIcon,
  },
  {
    title: "模型调试",
    description: "使用普通对话能力快速验证提示词和模型回复风格。",
    route: "/admin/model-playground",
    icon: CpuChipIcon,
  },
  {
    title: "用户管理",
    description: "查看全量用户，支持启停状态与 VIP 等级调整。",
    route: "/admin/users",
    icon: UsersIcon,
  },
  {
    title: "工单管理",
    description: "查看工单、调整状态、分配处理人。",
    route: "/admin/tickets",
    icon: TicketIcon,
  },
  {
    title: "管理端智能助手",
    description: "支持图表 URL/参数渲染，帮助运营快速分析数据。",
    route: "/admin/assistant",
    icon: SparklesIcon,
  },
  {
    title: "运营建议",
    description: "新功能开发中....",
    route: "/admin/knowledge-base",
    icon: DocumentMagnifyingGlassIcon,
  },
]);
</script>

<style scoped lang="scss">
.portal-home {
  min-height: calc(100vh - 64px);
  padding: 2rem;
  background: linear-gradient(135deg, #fafbff 0%, #f0f4ff 50%, #e8efff 100%);
}

.container {
  max-width: 1500px;
  margin: 0 auto;
}

.hero {
  padding: 2rem;
  border-radius: 1.5rem;
  background: #f8fafc;

  .eyebrow {
    color: #7c3aed;
    font-size: 0.85rem;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 0.08em;
  }

  h1 {
    margin: 0.5rem 0 0.85rem;
    font-size: 2.3rem;
  }

  p {
    max-width: 760px;
    color: #64748b;
    line-height: 1.7;
  }
}

.cards-grid {
  margin-top: 1.5rem;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 1rem;
}

.card {
  padding: 1.5rem;
  border-radius: 1.25rem;
  background: rgba(255, 255, 255, 0.92);
  color: inherit;
  text-decoration: none;
  box-shadow: 0 18px 32px rgba(15, 23, 42, 0.06);
  transition: all 0.3s cubic-bezier(0.17, 0.67, 0.88, 1.01); // 平滑过渡

  &:hover {
    transform: translateY(-8px); // 上浮 8px
    box-shadow:
      0 24px 40px rgba(15, 23, 42, 0.12),
      0 4px 12px rgba(124, 58, 237, 0.15); // 更强阴影 + 紫色光晕
    background: rgba(255, 255, 255, 0.96); // 轻微提亮背景
  }

  .icon {
    width: 2rem;
    height: 2rem;
    color: #7c3aed;
    margin-bottom: 1rem;
    transition: transform 0.3s ease;
  }

  &:hover .icon {
    transform: scale(1.1); // 图标也轻微放大
  }

  h2 {
    margin-bottom: 0.6rem;
    font-size: 1.2rem;
  }

  p {
    color: #64748b;
    line-height: 1.65;
  }
}

.dark {
  background: linear-gradient(135deg, #020617 0%, #0f172a 50%, #1e293b 100%);

  .hero {
    background: linear-gradient(
      135deg,
      rgba(124, 58, 237, 0.18),
      rgba(37, 99, 235, 0.14)
    );
  }

  .hero p,
  .card p {
    color: #94a3b8;
  }

  .card {
    background: rgba(30, 41, 59, 0.92);
    box-shadow: 0 18px 32px rgba(0, 0, 0, 0.3);

    &:hover {
      transform: translateY(-8px);
      box-shadow:
        0 24px 40px rgba(0, 0, 0, 0.4),
        0 4px 12px rgba(124, 58, 237, 0.25);
      background: rgba(30, 41, 59, 0.98);
    }

    .icon {
      transition: transform 0.3s ease;
    }

    &:hover .icon {
      transform: scale(1.1);
    }
  }
}

@media (max-width: 960px) {
  .portal-home {
    padding: 1rem;
  }

  .cards-grid {
    grid-template-columns: 1fr;
  }
}
</style>
