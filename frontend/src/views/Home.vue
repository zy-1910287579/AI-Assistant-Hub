<template>
  <div class="home" :class="{ dark: isDark }">
    <div class="container">
      <div class="hero">
        <div>
          <p class="eyebrow">User Portal</p>
          <h1 class="title">企业智能客服用户端</h1>
        </div>
      </div>
      <div class="cards-grid">
        <router-link
          v-for="app in aiApps"
          :key="app.id"
          :to="app.route"
          class="card"
        >
          <div class="card-content">
            <component :is="app.icon" class="icon" />
            <h2>{{ app.title }}</h2>
            <p>{{ app.description }}</p>
          </div>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useDark } from "@vueuse/core";
import {
  ChatBubbleLeftRightIcon,
  HeartIcon,
  UserGroupIcon,
  ShoppingBagIcon,
  UserCircleIcon,
  TicketIcon,
} from "@heroicons/vue/24/outline";

const isDark = useDark();

const aiApps = ref([
  {
    id: 1,
    title: "AI 聊天",
    description: "体验模型的普通问答能力",
    route: "/user/entertainment/ai-chat",
    icon: ChatBubbleLeftRightIcon,
  },
  {
    id: 2,
    title: "个人中心",
    description: "查看当前登录用户资料并修改密码",
    route: "/user/profile",
    icon: UserCircleIcon,
  },
  {
    id: 3,
    title: "初恋那些事",
    description: "一个帮助你弥补遗憾的小游戏",
    route: "/user/entertainment/game",
    icon: HeartIcon,
    iconClass: "heart-icon",
  },
  {
    id: 4,
    title: "小麦AI智能客服",
    description: "可进行意图识别，支持订单查询、售后知识问答和工单转交",
    route: "/user/customer-service",
    icon: UserGroupIcon,
  },
  {
    id: 5,
    title: "订单服务",
    description: "订单查询、物流追踪、退款售后等企业服务能力",
    route: "/user/orders",
    icon: ShoppingBagIcon,
  },
  {
    id: 6,
    title: "我的工单",
    description: "查看和取消我的待处理工单",
    route: "/user/tickets",
    icon: TicketIcon,
  },
]);
</script>

<style scoped lang="scss">
.home {
  min-height: 100vh;
  padding: 2rem;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 50%, #dbeafe 100%);
  transition: background-color 0.3s;

  .container {
    max-width: 1600px;
    margin: 0 auto;
    padding: 0 2rem;
  }

  .title {
    font-size: 2.5rem;
    margin-bottom: 0.75rem;
    background: linear-gradient(45deg, #007cf0, #00dfd8);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    animation: fadeIn 1s ease-out;
  }

  .hero {
    padding: 2rem;
    margin-bottom: 2rem;
    border-radius: 1.5rem;
    background: linear-gradient(
      135deg,
      rgba(37, 99, 235, 0.14),
      rgba(16, 185, 129, 0.1)
    );
  }

  .eyebrow {
    color: #2563eb;
    font-size: 0.85rem;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 0.08em;
  }

  .subtitle {
    max-width: 760px;
    color: #64748b;
    line-height: 1.7;
  }

  .cards-grid {
    display: grid;
    grid-template-columns: repeat(1, 1fr);
    gap: 2rem;
    justify-items: center;
    padding: 1rem;

    @media (min-width: 768px) {
      grid-template-columns: repeat(2, 1fr);
    }

    @media (min-width: 1200px) {
      grid-template-columns: repeat(4, 1fr);
    }
  }

  .card {
    position: relative;
    width: 100%;
    max-width: 320px;
    background: rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(10px);
    border-radius: 20px;
    padding: 2rem;
    text-decoration: none;
    color: inherit;
    transition: all 0.3s ease;
    border: 1px solid rgba(255, 255, 255, 0.1);
    overflow: hidden;

    .dark & {
      background: rgba(255, 255, 255, 0.05);
      border: 1px solid rgba(255, 255, 255, 0.05);
    }

    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);

      .dark & {
        box-shadow: 0 10px 20px rgba(0, 0, 0, 0.3);
      }
    }

    .card-content {
      display: flex;
      flex-direction: column;
      align-items: center;
      text-align: center;
    }

    .icon {
      width: 48px;
      height: 48px;
      margin-bottom: 1rem;
      color: #007cf0;

      &.heart-icon {
        color: #ff4d4f;
        animation: pulse 1.5s ease-in-out infinite;
      }
    }

    h2 {
      font-size: 1.5rem;
      margin-bottom: 0.5rem;
    }

    p {
      color: #666;
      font-size: 1rem;

      .dark & {
        color: #999;
      }
    }
  }

  &.dark {
    background: linear-gradient(135deg, #020617 0%, #0f172a 50%, #1e293b 100%);

    .card {
      background: rgba(255, 255, 255, 0.05);

      p {
        color: #999;
      }
    }

    .subtitle {
      color: #94a3b8;
    }
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
  }
}

@media (max-width: 768px) {
  .home {
    padding: 1rem;

    .container {
      padding: 0 1rem;
    }

    .title {
      font-size: 2rem;
    }

    .card {
      max-width: 100%;
    }
  }
}
</style>
