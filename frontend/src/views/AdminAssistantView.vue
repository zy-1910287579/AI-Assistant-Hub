<template>
  <div class="admin-assistant">
    <div class="layout">
      <aside class="sidebar">
        <div class="sidebar-header">
          <h3>助手会话</h3>
          <button class="new-chat" @click="startNewChat">新会话</button>
        </div>

        <div class="session-list">
          <div
            v-for="session in sessions"
            :key="session.sessionId"
            class="session-item"
            :class="{
              active: currentSessionId === session.sessionId,
              pinned: session.pinned,
            }"
            @click="switchSession(session.sessionId)"
          >
            <div class="session-title">
              {{ session.title || "会话 " + session.sessionId.slice(-4) }}
              <span v-if="session.pinned" class="pin-badge">置顶</span>
            </div>
            <div class="session-time">
              {{ formatTime(session.updatedAt) }}
            </div>
            <div class="session-actions">
              <button
                class="action-btn"
                @click.stop="togglePin(session)"
                title="置顶"
              >
                <span v-if="session.pinned">📌</span>
                <span v-else>📍</span>
              </button>
              <button
                class="action-btn"
                @click.stop="renameSession(session)"
                title="重命名"
              >
                ✏️
              </button>
              <button
                class="action-btn delete"
                @click.stop="deleteSession(session)"
                title="删除"
              >
                🗑️
              </button>
            </div>
          </div>
        </div>
      </aside>

      <section class="main-chat">
        <div class="chat-header">
          <h2>管理端智能助手</h2>
          <p>支持多图片 URL 与链接自动打开</p>
          <button
            class="clear-btn"
            :disabled="!currentSessionId || !messages.length"
            @click="clearHistory"
          >
            清空当前记录
          </button>
        </div>

        <div ref="messagesRef" class="messages">
          <div v-if="!messages.length" class="empty-state">
            <ChatBubbleLeftRightIcon class="empty-icon" />
            <p>开始智能对话吧</p>
          </div>
          <div
            v-for="(msg, idx) in messages"
            :key="idx"
            class="message-wrapper"
            :class="msg.role === 'user' ? 'message-user' : 'message-assistant'"
          >
            <div class="message-content">
              <div v-if="msg.role === 'user'" class="message-text">
                {{ msg.content }}
              </div>
              <div
                v-else
                class="message-text markdown-content"
                v-html="renderMarkdown(msg.content)"
              ></div>
            </div>
            <div v-if="msg.timestamp" class="message-time">
              {{ formatMessageTime(msg.timestamp) }}
            </div>
          </div>
        </div>

        <div class="input-area">
          <textarea
            ref="inputRef"
            v-model="prompt"
            placeholder="例如：统计近 7 天工单状态并展示柱状图"
            @keydown.enter.exact.prevent="send"
            @input="adjustTextareaHeight"
          ></textarea>
          <button :disabled="loading || !prompt.trim()" @click="send">
            {{ loading ? "发送中..." : "发送" }}
          </button>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onMounted, ref } from "vue";
import { useDark } from "@vueuse/core";
import { marked } from "marked";
import DOMPurify from "dompurify";
import {
  EllipsisHorizontalIcon,
  ChatBubbleLeftRightIcon,
} from "@heroicons/vue/24/outline";
import { chatAPI } from "../services/api";
import {
  createSession,
  getActiveSessionId,
  getSessionsByScope,
  setActiveSessionId,
  upsertSession,
  removeSession,
} from "../utils/sessionStore";

const isDark = useDark();
const SESSION_SCOPE = "admin-assistant";
const prompt = ref("");
const loading = ref(false);
const messages = ref([]);
const sessions = ref([]);
const currentSessionId = ref("");
const messagesRef = ref(null);
const inputRef = ref(null);

const renderMarkdown = (content) => {
  return DOMPurify.sanitize(marked.parse(content));
};

const formatMessageTime = (timestamp) => {
  if (!timestamp) return "";
  const date = new Date(timestamp);
  const year = date.getFullYear();
  const month = (date.getMonth() + 1).toString().padStart(2, "0");
  const day = date.getDate().toString().padStart(2, "0");
  const hours = date.getHours().toString().padStart(2, "0");
  const minutes = date.getMinutes().toString().padStart(2, "0");
  return `${year}-${month}-${day} ${hours}:${minutes}`;
};

const scrollToBottom = async () => {
  await nextTick();
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight;
  }
};

const loadHistory = async () => {
  try {
    const history = await chatAPI.getHistoryMessages({
      sessionId: currentSessionId.value,
    });
    messages.value = history.map((item) => ({
      role: item.role,
      content: item.content,
      timestamp: item.timestamp,
    }));
  } catch {
    messages.value = [];
  } finally {
    await scrollToBottom();
  }
};

const clearHistory = async () => {
  if (!currentSessionId.value) {
    messages.value.push({
      role: "assistant",
      content: "会话未初始化",
    });
    return;
  }

  try {
    await chatAPI.clearHistory({ sessionId: currentSessionId.value });
    messages.value = [];
  } catch (e) {
    messages.value.push({
      role: "assistant",
      content: e?.message || "清空失败",
    });
  }
};

const send = async () => {
  const value = prompt.value.trim();
  if (!value || loading.value) return;

  const sessionId = currentSessionId.value;

  if (!sessionId) {
    messages.value.push({
      role: "assistant",
      content: "会话未初始化，请刷新页面或新建会话",
    });
    return;
  }

  messages.value.push({ role: "user", content: value, timestamp: new Date() });
  prompt.value = "";
  loading.value = true;
  await scrollToBottom();

  const assistantIndex = messages.value.length;
  messages.value.push({
    role: "assistant",
    content: "",
    timestamp: new Date(),
  });

  try {
    const res = await chatAPI.streamAdminChat({ prompt: value, sessionId });
    const decoder = new TextDecoder("utf-8");
    let content = "";

    while (true) {
      const { value, done } = await res.read();
      if (done) break;
      content += decoder.decode(value, { stream: true });
      messages.value[assistantIndex] = {
        role: "assistant",
        content,
        timestamp: new Date(),
      };
      await scrollToBottom();
    }
  } catch (e) {
    messages.value[assistantIndex] = {
      role: "assistant",
      content: e?.message || "请求失败",
      timestamp: new Date(),
    };
  } finally {
    loading.value = false;
    await scrollToBottom();
  }
};

const syncSessions = () => {
  sessions.value = getSessionsByScope(SESSION_SCOPE);
};

const startNewChat = async () => {
  const session = createSession(SESSION_SCOPE);
  currentSessionId.value = session.sessionId;
  messages.value = [];
  setActiveSessionId(SESSION_SCOPE, session.sessionId);
  syncSessions();
};

const switchSession = async (sessionId) => {
  currentSessionId.value = sessionId;
  setActiveSessionId(SESSION_SCOPE, sessionId);
  messages.value = [];
  await loadHistory();
  syncSessions();
};

const togglePin = (session) => {
  upsertSession(SESSION_SCOPE, {
    ...session,
    pinned: !session.pinned,
    updatedAt: Date.now(),
  });
  syncSessions();
};

const renameSession = (session) => {
  const newTitle = prompt("请输入新标题：", session.title);
  if (newTitle && newTitle.trim()) {
    upsertSession(SESSION_SCOPE, {
      ...session,
      title: newTitle.trim(),
      updatedAt: Date.now(),
    });
    syncSessions();
  }
};

const deleteSession = async (session) => {
  if (!confirm(`确定删除会话 "${session.title || session.sessionId}" 吗？`))
    return;

  try {
    await chatAPI.clearHistory({ sessionId: session.sessionId });
  } catch (error) {
    console.error("删除后端历史失败:", error);
  }

  removeSession(SESSION_SCOPE, session.sessionId);
  syncSessions();

  if (currentSessionId.value === session.sessionId) {
    const remaining = getSessionsByScope(SESSION_SCOPE);
    if (remaining.length > 0) {
      switchSession(remaining[0].sessionId);
    } else {
      startNewChat();
    }
  }
};

const formatTime = (ts) => {
  if (!ts) return "";
  const d = new Date(ts);
  const now = new Date();
  const diff = now - d;
  if (diff < 60000) return "刚刚";
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`;
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`;
  return d.toLocaleDateString();
};

const adjustTextareaHeight = () => {
  if (!inputRef.value) return;
  inputRef.value.style.height = "auto";
  inputRef.value.style.height =
    Math.min(inputRef.value.scrollHeight, 150) + "px";
};

onMounted(async () => {
  syncSessions();
  const existing = getActiveSessionId(SESSION_SCOPE);
  if (existing) {
    currentSessionId.value = existing;
    await loadHistory();
  } else if (sessions.value.length > 0) {
    await switchSession(sessions.value[0].sessionId);
  } else {
    await startNewChat();
  }
  adjustTextareaHeight();
});
</script>

<style scoped lang="scss">
.admin-assistant {
  position: fixed;
  top: 64px;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 1.5rem 2rem;
  background: var(--bg-color);

  .layout {
    max-width: 1800px;
    height: 100%;
    margin: 0 auto;
    display: grid;
    grid-template-columns: 280px 1fr 280px;
    gap: 1rem;
  }

  .sidebar,
  .main-chat,
  .chart-panel {
    background: rgba(255, 255, 255, 0.94);
    backdrop-filter: blur(10px);
    border-radius: 1.25rem;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  }

  .sidebar {
    padding: 1rem;
    display: flex;
    flex-direction: column;

    .sidebar-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1rem;

      h3 {
        margin: 0;
        font-size: 1.1rem;
      }

      .new-chat {
        background: #007cf0;
        color: white;
        border: none;
        padding: 0.5rem 0.8rem;
        border-radius: 0.5rem;
        cursor: pointer;
        font-size: 0.85rem;

        &:hover {
          background: #0066cc;
        }
      }
    }

    .session-list {
      flex: 1;
      overflow-y: auto;
      display: flex;
      flex-direction: column;
      gap: 0.5rem;

      .session-item {
        padding: 0.75rem;
        border-radius: 0.75rem;
        background: rgba(255, 255, 255, 0.6);
        cursor: pointer;
        transition: all 0.2s;
        position: relative;

        &:hover {
          background: rgba(255, 255, 255, 0.85);
        }

        &.active {
          background: rgba(0, 124, 240, 0.12);
          border: 1px solid rgba(0, 124, 240, 0.3);
        }

        &.pinned {
          border-left: 3px solid #f59e0b;
        }

        .session-title {
          font-weight: 600;
          font-size: 0.95rem;
          margin-bottom: 0.25rem;
          display: flex;
          align-items: center;
          gap: 0.5rem;

          .pin-badge {
            font-size: 0.7rem;
            background: #f59e0b;
            color: white;
            padding: 0.1rem 0.3rem;
            border-radius: 0.25rem;
          }
        }

        .session-time {
          font-size: 0.75rem;
          opacity: 0.6;
        }

        .session-actions {
          position: absolute;
          right: 0.5rem;
          top: 0.5rem;
          display: flex;
          gap: 0.25rem;
          opacity: 0;
          transition: opacity 0.2s;

          .action-btn {
            width: 1.5rem;
            height: 1.5rem;
            border: none;
            background: rgba(255, 255, 255, 0.9);
            border-radius: 0.25rem;
            cursor: pointer;
            font-size: 0.8rem;
            display: grid;
            place-items: center;

            &:hover {
              background: white;
            }

            &.delete:hover {
              background: #fee2e2;
            }
          }
        }

        &:hover .session-actions {
          opacity: 1;
        }
      }
    }
  }

  .main-chat {
    display: flex;
    flex-direction: column;
    padding: 1.5rem;
    height: 100%;
    min-height: 0;

    .chat-header {
      flex-shrink: 0;
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1rem;

      h2 {
        margin: 0;
        font-size: 1.3rem;
      }

      p {
        margin: 0;
        opacity: 0.7;
        font-size: 0.9rem;
      }

      .clear-btn {
        background: rgba(239, 68, 68, 0.12);
        color: #ef4444;
        border: none;
        padding: 0.5rem 0.8rem;
        border-radius: 0.5rem;
        cursor: pointer;
        font-size: 0.85rem;

        &:hover {
          background: rgba(239, 68, 68, 0.2);
        }
      }
    }

    .messages {
      flex: 1;
      overflow-y: auto;
      padding: 0.5rem;
      display: flex;
      flex-direction: column;
      gap: 1rem;
      min-height: 0;

      .empty-state {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        opacity: 0.3;

        .empty-icon {
          width: 8rem;
          height: 8rem;
          margin-bottom: 1.5rem;
          opacity: 0.5;
        }

        p {
          margin: 0;
          font-size: 1.1rem;
          color: #9ca3af;
        }
      }

      .message-wrapper {
        display: flex;
        gap: 0.75rem;
        align-items: flex-start;

        &.message-user {
          flex-direction: column;
          align-items: flex-end;

          .message-content {
            background: rgba(248, 250, 252, 0.95);
            color: #0f172a;
          }

          .message-time {
            margin-top: 0.25rem;
            font-size: 0.75rem;
            color: #999;
          }
        }

        &.message-assistant {
          flex-direction: row;
          align-items: flex-start;

          .message-content {
            background: rgba(248, 250, 252, 0.95);
            color: #0f172a;
          }

          .message-time {
            margin-top: 0.25rem;
            font-size: 0.75rem;
            color: #999;
          }
        }

        .message-content {
          max-width: 70%;
          padding: 0.85rem 1rem;
          border-radius: 1rem;
          word-break: break-word;

          .message-text {
            margin: 0;
            line-height: 1.6;
            white-space: pre-wrap;
          }
        }
      }

      .markdown-content {
        h1,
        h2,
        h3,
        h4,
        h5,
        h6 {
          margin: 1rem 0 0.5rem;
          font-weight: 600;
          line-height: 1.25;

          &:first-child {
            margin-top: 0;
          }

          &:last-child {
            margin-bottom: 0;
          }
        }

        h1 {
          font-size: 1.5rem;
        }
        h2 {
          font-size: 1.25rem;
        }
        h3 {
          font-size: 1.1rem;
        }
        h4 {
          font-size: 1rem;
        }

        p {
          margin: 0.5rem 0;
          line-height: 1.6;

          &:first-child {
            margin-top: 0;
          }

          &:last-child {
            margin-bottom: 0;
          }
        }

        ul,
        ol {
          margin: 0.5rem 0;
          padding-left: 1.5rem;

          &:first-child {
            margin-top: 0;
          }

          &:last-child {
            margin-bottom: 0;
          }
        }

        li {
          margin: 0.25rem 0;
          line-height: 1.5;
        }

        code {
          background: rgba(0, 0, 0, 0.06);
          padding: 0.2rem 0.4rem;
          border-radius: 0.25rem;
          font-family: "Consolas", "Monaco", monospace;
          font-size: 0.85em;
        }

        pre {
          background: #0d1117;
          padding: 1rem;
          border-radius: 0.5rem;
          overflow-x: auto;
          margin: 0.5rem 0;

          code {
            background: transparent;
            padding: 0;
            color: #c9d1d9;
          }
        }

        blockquote {
          border-left: 3px solid rgba(0, 0, 0, 0.2);
          padding-left: 1rem;
          margin: 0.5rem 0;
          color: #6b7280;
          font-style: italic;
        }

        table {
          border-collapse: collapse;
          width: 100%;
          margin: 0.5rem 0;

          th,
          td {
            border: 1px solid rgba(0, 0, 0, 0.1);
            padding: 0.5rem;
            text-align: left;
          }

          th {
            background: rgba(0, 0, 0, 0.05);
            font-weight: 600;
          }
        }

        a {
          color: #007cf0;
          text-decoration: underline;

          &:hover {
            color: #0056b3;
          }
        }
      }
    }

    .input-area {
      flex-shrink: 0;
      display: flex;
      gap: 0.75rem;
      align-items: flex-end;
      margin-top: 1rem;

      textarea {
        flex: 1;
        resize: none;
        border: 1px solid rgba(0, 0, 0, 0.1);
        background: white;
        border-radius: 0.75rem;
        padding: 0.75rem;
        color: inherit;
        font-family: inherit;
        font-size: 0.95rem;
        line-height: 1.5;
        min-height: 3rem;
        max-height: 150px;

        &:focus {
          outline: none;
          border-color: #007cf0;
          box-shadow: 0 0 0 2px rgba(0, 124, 240, 0.1);
        }
      }

      button {
        background: #007cf0;
        color: white;
        border: none;
        border-radius: 0.5rem;
        padding: 0.75rem 1.25rem;
        cursor: pointer;
        font-weight: 600;
        transition: background-color 0.3s;
        flex-shrink: 0;

        &:hover:not(:disabled) {
          background: #0066cc;
        }

        &:disabled {
          background: #ccc;
          cursor: not-allowed;
        }
      }
    }
  }
}

.dark {
  background: linear-gradient(135deg, #020617 0%, #0f172a 100%);

  .sidebar,
  .main-chat {
    background: rgba(30, 30, 30, 0.95);
  }

  .sidebar {
    .session-item {
      background: rgba(255, 255, 255, 0.05);
      color: #60a5fa;

      &:hover {
        background: rgba(255, 255, 255, 0.1);
      }

      &.active {
        background: rgba(96, 165, 250, 0.2);
        border-color: rgba(96, 165, 250, 0.5);
      }

      .session-actions .action-btn {
        background: rgba(50, 50, 50, 0.9);

        &:hover {
          background: rgba(255, 255, 255, 0.1);
        }
      }
    }
  }

  .main-chat {
    .input-area {
      textarea {
        background: rgba(50, 50, 50, 0.95);
        border-color: rgba(255, 255, 255, 0.1);
        color: #60a5fa;

        &::placeholder {
          color: rgba(96, 165, 250, 0.5);
        }

        &:focus {
          border-color: #60a5fa;
          box-shadow: 0 0 0 2px rgba(96, 165, 250, 0.2);
        }
      }
    }
  }

  .chart-panel {
    .link-list {
      .link-item {
        background: rgba(96, 165, 250, 0.15);
        color: #93c5fd;

        &:hover {
          background: rgba(96, 165, 250, 0.25);
        }
      }
    }
  }
}
</style>
