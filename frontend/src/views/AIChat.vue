<template>
  <div class="ai-chat" :class="{ dark: isDark }">
    <div class="chat-shell">
      <div class="chat-header">
        <div>
          <h1>普通对话</h1>
        </div>
        <button
          class="clear-btn"
          :disabled="isLoading || !currentMessages.length"
          @click="clearMessages"
        >
          清空对话
        </button>
      </div>

      <div ref="messagesRef" class="messages">
        <div v-if="!currentMessages.length" class="empty-state">
          <ChatBubbleLeftRightIcon class="empty-icon" />
          <h2>开始一次普通对话</h2>
          <p></p>
        </div>
        <div
          v-for="(message, index) in currentMessages"
          :key="index"
          class="message-wrapper"
          :class="
            message.role === 'user' ? 'message-user' : 'message-assistant'
          "
        >
          <div class="message-content">
            <div class="message-text" v-if="message.role === 'user'">
              {{ message.content }}
            </div>
            <div
              class="message-text markdown-content"
              v-else
              v-html="renderMarkdown(message.content)"
            ></div>
          </div>
          <div v-if="message.timestamp" class="message-time">
            {{ formatMessageTime(message.timestamp) }}
          </div>
        </div>
      </div>

      <div class="input-area">
        <textarea
          ref="inputRef"
          v-model="userInput"
          rows="1"
          placeholder="请输入你的问题..."
          @input="adjustTextareaHeight"
          @keydown.enter.prevent="sendMessage"
        ></textarea>
        <button
          class="send-btn"
          :disabled="isLoading || !userInput.trim()"
          @click="sendMessage"
        >
          <PaperAirplaneIcon class="icon" />
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { nextTick, onMounted, ref } from "vue";
import { useDark } from "@vueuse/core";
import { marked } from "marked";
import DOMPurify from "dompurify";
import {
  PaperAirplaneIcon,
  ChatBubbleLeftRightIcon,
} from "@heroicons/vue/24/outline";
import ChatMessage from "../components/ChatMessage.vue";
import { chatAPI } from "../services/api";
import {
  createSession,
  getActiveSessionId,
  setActiveSessionId,
} from "../utils/sessionStore";

const renderMarkdown = (content) => {
  return DOMPurify.sanitize(marked.parse(content));
};

const isDark = useDark();
const messagesRef = ref(null);
const inputRef = ref(null);
const userInput = ref("");
const isLoading = ref(false);
const currentMessages = ref([]);
const sessionId = ref("");
const SESSION_SCOPE = "plain";

const adjustTextareaHeight = () => {
  const textarea = inputRef.value;

  if (!textarea) {
    return;
  }

  textarea.style.height = "auto";
  textarea.style.height = `${Math.min(textarea.scrollHeight, 160)}px`;
};

const scrollToBottom = async () => {
  await nextTick();

  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight;
  }
};

const loadHistory = async () => {
  if (!sessionId.value) return;

  try {
    const history = await chatAPI.getHistoryMessages({
      sessionId: sessionId.value,
    });
    currentMessages.value = history;
    await scrollToBottom();
  } catch (error) {
    console.error("加载历史消息失败:", error);
    currentMessages.value = [];
  }
};

const clearMessages = async () => {
  try {
    if (sessionId.value) {
      await chatAPI.clearHistory({ sessionId: sessionId.value });
    }
    currentMessages.value = [];
    userInput.value = "";
    adjustTextareaHeight();
  } catch (error) {
    console.error("清空历史失败:", error);
  }
};

const ensureSession = () => {
  if (sessionId.value) return sessionId.value;
  const existing = getActiveSessionId(SESSION_SCOPE);
  if (existing) {
    sessionId.value = existing;
    return existing;
  }
  const created = createSession(SESSION_SCOPE);
  sessionId.value = created.sessionId;
  setActiveSessionId(SESSION_SCOPE, created.sessionId);
  return created.sessionId;
};

const sendMessage = async () => {
  const prompt = userInput.value.trim();

  if (!prompt || isLoading.value) {
    return;
  }

  currentMessages.value.push({
    role: "user",
    content: prompt,
    timestamp: new Date(),
  });

  userInput.value = "";
  adjustTextareaHeight();

  currentMessages.value.push({
    role: "assistant",
    content: "",
    timestamp: new Date(),
    isMarkdown: true,
  });

  const assistantIndex = currentMessages.value.length - 1;
  isLoading.value = true;
  await scrollToBottom();

  try {
    const reader = await chatAPI.streamTalk({
      prompt,
      sessionId: ensureSession(),
    });
    const decoder = new TextDecoder("utf-8");
    let content = "";
    while (true) {
      const { value, done } = await reader.read();
      if (done) break;
      content += decoder.decode(value, { stream: true });
      currentMessages.value[assistantIndex] = {
        role: "assistant",
        content,
        timestamp: new Date(),
        isMarkdown: true,
      };
      await scrollToBottom();
    }
  } catch (error) {
    if (!currentMessages.value[assistantIndex].content) {
      try {
        const content = await chatAPI.talk(prompt);
        currentMessages.value[assistantIndex] = {
          role: "assistant",
          content,
          timestamp: new Date(),
          isMarkdown: true,
        };
      } catch (innerError) {
        currentMessages.value[assistantIndex] = {
          role: "assistant",
          content: innerError.message || "请求失败，请稍后重试。",
          timestamp: new Date(),
          isMarkdown: true,
        };
      }
    } else {
      currentMessages.value[assistantIndex] = {
        role: "assistant",
        content: error.message || "请求失败，请稍后重试。",
        timestamp: new Date(),
        isMarkdown: true,
      };
    }
  } finally {
    isLoading.value = false;
    await scrollToBottom();
  }
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

onMounted(async () => {
  adjustTextareaHeight();

  const existing = getActiveSessionId(SESSION_SCOPE);
  if (existing) {
    sessionId.value = existing;
    await loadHistory();
  } else {
    const newSessionId = ensureSession();
    sessionId.value = newSessionId;
    await loadHistory();
  }
});
</script>

<style scoped lang="scss">
.ai-chat {
  position: fixed;
  top: 64px;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 1.5rem 2rem;
  background: var(--bg-color);

  .chat-shell {
    max-width: 1200px;
    height: 100%;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    gap: 1rem;
    padding: 1.5rem;
    border-radius: 1.25rem;
    background: rgba(255, 255, 255, 0.92);
    backdrop-filter: blur(10px);
  }

  .chat-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 1rem;

    h1 {
      font-size: 1.5rem;
      margin-bottom: 0.35rem;
    }

    p {
      color: #666;
    }
  }

  .clear-btn {
    padding: 0.65rem 1rem;
    border: none;
    border-radius: 0.75rem;
    background: rgba(15, 23, 42, 0.08);
    color: inherit;
    cursor: pointer;
  }

  .clear-btn:disabled {
    cursor: not-allowed;
    opacity: 0.5;
  }

  .messages {
    flex: 1;
    overflow-y: auto;
    padding: 0.5rem 0.25rem;

    .message-wrapper {
      display: flex;
      margin-bottom: 1rem;
      padding: 0 0.5rem;

      &.message-user {
        justify-content: flex-end;
        flex-direction: column;
        align-items: flex-end;

        .message-content {
          background: rgba(248, 250, 252, 0.95);
          color: #0f172a;
          max-width: 70%;
          padding: 0.85rem 1rem;
          border-radius: 1rem 1rem 0 1rem;
          word-break: break-word;

          .message-text {
            margin: 0;
            line-height: 1.6;
            white-space: pre-wrap;
          }
        }

        .message-time {
          margin-top: 0.25rem;
          font-size: 0.75rem;
          color: #999;
        }
      }

      &.message-assistant {
        justify-content: flex-start;
        flex-direction: column;
        align-items: flex-start;

        .message-content {
          background: rgba(248, 250, 252, 0.95);
          color: #0f172a;
          max-width: 70%;
          padding: 0.85rem 1rem;
          border-radius: 1rem 1rem 1rem 0;
          word-break: break-word;

          .message-text {
            margin: 0;
            line-height: 1.6;
            white-space: pre-wrap;
          }
        }

        .message-time {
          margin-top: 0.25rem;
          font-size: 0.75rem;
          color: #999;
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
  }

  .empty-state {
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-align: center;
    color: #666;
    opacity: 0.3;

    .empty-icon {
      width: 8rem;
      height: 8rem;
      margin-bottom: 1.5rem;
      opacity: 0.5;
    }

    h2 {
      margin-bottom: 0.75rem;
      margin-top: 0;
    }
  }

  .input-area {
    display: flex;
    gap: 0.75rem;
    align-items: flex-end;
  }

  textarea {
    flex: 1;
    resize: none;
    min-height: 56px;
    max-height: 160px;
    padding: 1rem 1.1rem;
    border: 1px solid rgba(15, 23, 42, 0.08);
    border-radius: 1rem;
    background: rgba(248, 250, 252, 0.95);
    color: inherit;
    outline: none;
    font: inherit;
  }

  .send-btn {
    width: 56px;
    height: 56px;
    border: none;
    border-radius: 1rem;
    background: #2563eb;
    color: white;
    cursor: pointer;
  }

  .send-btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }

  .icon {
    width: 1.35rem;
    height: 1.35rem;
  }

  &.dark {
    color: #60a5fa;

    .chat-shell {
      background: rgba(17, 24, 39, 0.82);
    }

    .chat-header h2 {
      color: #93c5fd;
    }

    .chat-header p,
    .empty-state {
      color: #9ca3af;
    }

    .session-list .session-item {
      background: rgba(255, 255, 255, 0.05);
      color: #60a5fa;

      &:hover {
        background: rgba(255, 255, 255, 0.1);
      }

      &.active {
        background: rgba(96, 165, 250, 0.2);
        border-color: rgba(96, 165, 250, 0.5);
      }
    }

    textarea {
      background: rgba(15, 23, 42, 0.65);
      border-color: rgba(255, 255, 255, 0.08);
      color: #60a5fa;

      &::placeholder {
        color: rgba(96, 165, 250, 0.5);
      }
    }

    .clear-btn {
      background: rgba(255, 255, 255, 0.08);
      color: #f87171;
    }
  }
}

@media (max-width: 768px) {
  .ai-chat {
    padding: 1rem;

    .chat-shell {
      padding: 1rem;
    }

    .chat-header {
      flex-direction: column;
      align-items: flex-start;
    }

    .clear-btn {
      width: 100%;
    }
  }
}
</style>
