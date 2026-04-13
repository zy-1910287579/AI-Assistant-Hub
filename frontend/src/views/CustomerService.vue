<template>
  <div class="customer-service" :class="{ dark: isDark }">
    <div class="chat-container">
      <aside class="sidebar">
        <div class="sidebar-header">
          <h2>咨询记录</h2>
          <button class="primary-btn" @click="startNewChat">新咨询</button>
        </div>
        <div class="history-list">
          <div
            v-for="session in chatHistory"
            :key="session.sessionId"
            class="history-item"
            :class="{ active: currentSessionId === session.sessionId }"
          >
            <ChatBubbleLeftRightIcon class="icon" />
            <button
              class="history-main"
              @click="loadSession(session.sessionId)"
            >
              <span class="history-label">
                {{ session.title }}
              </span>
              <span v-if="session.pinned" class="pin-badge">置顶</span>
            </button>
            <button
              class="session-action"
              @click.stop="toggleSessionMenu(session.sessionId)"
            >
              <EllipsisHorizontalIcon class="menu-icon" />
            </button>
            <div
              v-if="activeMenuSessionId === session.sessionId"
              class="session-menu"
            >
              <button @click.stop="renameSession(session)">重命名</button>
              <button @click.stop="toggleSessionPinned(session)">
                {{ session.pinned ? "取消置顶" : "置顶" }}
              </button>
              <button
                class="danger-item"
                :disabled="
                  isStreaming && currentSessionId === session.sessionId
                "
                @click.stop="deleteSessionItem(session)"
              >
                删除
              </button>
            </div>
          </div>
        </div>
      </aside>

      <section class="chat-main">
        <div class="service-header">
          <div class="service-info">
            <ComputerDesktopIcon class="avatar" />
            <div>
              <h3>小麦</h3>
              <p>小麦智能客服</p>
            </div>
          </div>
          <button
            class="secondary-btn"
            :disabled="isStreaming || !currentSessionId"
            @click="clearCurrentHistory"
          >
            清空当前记录
          </button>
        </div>

        <div ref="messagesRef" class="messages">
          <div v-if="!currentMessages.length" class="empty-state">
            <ChatBubbleLeftRightIcon class="empty-icon" />
            <h2>开始智能客服对话</h2>
          </div>
          <ChatMessage
            v-for="(message, index) in currentMessages"
            :key="index"
            :message="message"
            :is-stream="isStreaming && index === currentMessages.length - 1"
          />
        </div>

        <div class="input-area">
          <textarea
            ref="inputRef"
            v-model="userInput"
            rows="1"
            placeholder="请输入您的问题..."
            @input="adjustTextareaHeight"
            @keydown.enter.prevent="sendMessage()"
          ></textarea>
          <button
            class="send-btn"
            :disabled="isStreaming || !userInput.trim()"
            @click="sendMessage()"
          >
            <PaperAirplaneIcon class="icon" />
          </button>
        </div>
      </section>
    </div>

    <div v-if="showTicketModal" class="ticket-modal">
      <div class="modal-content">
        <h3>工单生成成功</h3>
        <div class="ticket-info" v-html="ticketInfo"></div>
        <button @click="showTicketModal = false">确定</button>
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
  ChatBubbleLeftRightIcon,
  ComputerDesktopIcon,
  EllipsisHorizontalIcon,
  PaperAirplaneIcon,
} from "@heroicons/vue/24/outline";
import ChatMessage from "../components/ChatMessage.vue";
import { chatAPI } from "../services/api";
import {
  createSession,
  getActiveSessionId,
  getSessionsByScope,
  getStoredUserId,
  removeSession,
  setActiveSessionId,
  upsertSession,
} from "../utils/sessionStore";

const SESSION_SCOPE = "customer";

marked.setOptions({
  breaks: true,
  gfm: true,
  sanitize: false,
});

const isDark = useDark();
const messagesRef = ref(null);
const inputRef = ref(null);
const userInput = ref("");
const isStreaming = ref(false);
const currentSessionId = ref("");
const currentMessages = ref([]);
const chatHistory = ref([]);
const showTicketModal = ref(false);
const ticketInfo = ref("");
const activeMenuSessionId = ref("");

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

const syncChatHistory = () => {
  chatHistory.value = getSessionsByScope(SESSION_SCOPE);
  if (
    activeMenuSessionId.value &&
    !chatHistory.value.some(
      (session) => session.sessionId === activeMenuSessionId.value,
    )
  ) {
    activeMenuSessionId.value = "";
  }
};

const buildSessionTitle = (prompt) => {
  const normalizedPrompt = prompt.trim().replace(/\s+/g, " ");
  return normalizedPrompt.length > 18
    ? `${normalizedPrompt.slice(0, 18)}...`
    : normalizedPrompt;
};

const extractTicketInfo = (content) => {
  if (!content.includes("工单编号") && !content.includes("工单生成成功")) {
    return "";
  }

  const matched = content.match(/【([\s\S]*?)】/);
  const source = matched?.[1] || content;

  return DOMPurify.sanitize(marked.parse(source), {
    ADD_TAGS: ["code", "pre", "span"],
    ADD_ATTR: ["class", "language"],
  });
};

const ensureSession = () => {
  if (currentSessionId.value) {
    return currentSessionId.value;
  }

  const session = createSession(SESSION_SCOPE);
  syncChatHistory();
  currentSessionId.value = session.sessionId;
  return session.sessionId;
};

const loadSession = async (sessionId) => {
  activeMenuSessionId.value = "";
  currentSessionId.value = sessionId;
  setActiveSessionId(SESSION_SCOPE, sessionId);
  showTicketModal.value = false;

  try {
    const history = await chatAPI.getHistoryMessages({
      userId: getStoredUserId(),
      sessionId,
    });
    // 确保每条消息都有时间戳
    currentMessages.value = history.map((msg) => ({
      ...msg,
      timestamp: msg.timestamp || new Date(),
    }));
  } catch (error) {
    currentMessages.value = [
      {
        role: "assistant",
        content: error.message || "加载历史记录失败，请稍后重试。",
        timestamp: new Date(),
        isMarkdown: true,
      },
    ];
  } finally {
    await scrollToBottom();
  }
};

const toggleSessionMenu = (sessionId) => {
  activeMenuSessionId.value =
    activeMenuSessionId.value === sessionId ? "" : sessionId;
};

const renameSession = (session) => {
  const nextTitle = window.prompt("请输入新的会话名称", session.title)?.trim();

  if (!nextTitle) {
    return;
  }

  upsertSession(SESSION_SCOPE, {
    ...session,
    title: nextTitle,
  });
  syncChatHistory();
  activeMenuSessionId.value = "";
};

const toggleSessionPinned = (session) => {
  upsertSession(SESSION_SCOPE, {
    ...session,
    pinned: !session.pinned,
  });
  syncChatHistory();
  activeMenuSessionId.value = "";
};

const deleteSessionItem = async (session) => {
  if (isStreaming.value && currentSessionId.value === session.sessionId) {
    return;
  }

  if (!window.confirm(`确认删除会话「${session.title}」吗？`)) {
    return;
  }

  try {
    await chatAPI.clearHistory({
      userId: getStoredUserId(),
      sessionId: session.sessionId,
    });
  } catch (error) {
    currentMessages.value.push({
      role: "assistant",
      content: error.message || "删除会话失败，请稍后重试。",
      timestamp: new Date(),
      isMarkdown: true,
    });
    await scrollToBottom();
    return;
  }

  removeSession(SESSION_SCOPE, session.sessionId);
  syncChatHistory();
  activeMenuSessionId.value = "";

  if (currentSessionId.value !== session.sessionId) {
    return;
  }

  currentMessages.value = [];
  showTicketModal.value = false;
  const nextSessionId = getActiveSessionId(SESSION_SCOPE);

  if (nextSessionId) {
    await loadSession(nextSessionId);
    return;
  }

  await startNewChat();
};

const startNewChat = async () => {
  const session = createSession(SESSION_SCOPE);
  syncChatHistory();
  currentSessionId.value = session.sessionId;
  currentMessages.value = [];
  userInput.value = "";
  showTicketModal.value = false;
  adjustTextareaHeight();
  await scrollToBottom();
};

const clearCurrentHistory = async () => {
  if (!currentSessionId.value || isStreaming.value) {
    return;
  }

  try {
    await chatAPI.clearHistory({
      userId: getStoredUserId(),
      sessionId: currentSessionId.value,
    });
    currentMessages.value = [];
    showTicketModal.value = false;
  } catch (error) {
    currentMessages.value.push({
      role: "assistant",
      content: error.message || "清空历史失败，请稍后重试。",
      timestamp: new Date(),
      isMarkdown: true,
    });
    await scrollToBottom();
  }
};

const sendMessage = async (presetContent) => {
  const prompt = (presetContent || userInput.value).trim();

  if (!prompt || isStreaming.value) {
    return;
  }

  const sessionId = ensureSession();

  currentMessages.value.push({
    role: "user",
    content: prompt,
    timestamp: new Date(),
  });

  if (!presetContent) {
    userInput.value = "";
    adjustTextareaHeight();
  }

  currentMessages.value.push({
    role: "assistant",
    content: "",
    timestamp: new Date(),
    isMarkdown: true,
  });

  const assistantIndex = currentMessages.value.length - 1;
  isStreaming.value = true;
  await scrollToBottom();

  try {
    const reader = await chatAPI.streamCustomerChat({
      prompt,
      userId: getStoredUserId(),
      sessionId,
    });
    const decoder = new TextDecoder("utf-8");
    let content = "";

    while (true) {
      const { value, done } = await reader.read();

      if (done) {
        break;
      }

      content += decoder.decode(value, { stream: true });
      currentMessages.value[assistantIndex] = {
        role: "assistant",
        content,
        timestamp: new Date(),
        isMarkdown: true,
      };
      await scrollToBottom();
    }

    const existingSession = chatHistory.value.find(
      (session) => session.sessionId === sessionId,
    );
    upsertSession(SESSION_SCOPE, {
      ...existingSession,
      sessionId,
      title: existingSession?.title?.startsWith("咨询 ")
        ? buildSessionTitle(prompt)
        : existingSession?.title || buildSessionTitle(prompt),
    });
    syncChatHistory();

    const nextTicketInfo = extractTicketInfo(content);
    if (nextTicketInfo) {
      ticketInfo.value = nextTicketInfo;
      showTicketModal.value = true;
    }
  } catch (error) {
    currentMessages.value[assistantIndex] = {
      role: "assistant",
      content: error.message || "抱歉，发生了错误，请稍后重试。",
      timestamp: new Date(),
      isMarkdown: true,
    };
  } finally {
    isStreaming.value = false;
    await scrollToBottom();
  }
};

const initializePage = async () => {
  syncChatHistory();
  const activeSessionId = getActiveSessionId(SESSION_SCOPE);

  // 优先使用活动会话 ID，如果没有则使用第一个会话
  const targetSessionId = activeSessionId || chatHistory.value[0]?.sessionId;

  if (targetSessionId) {
    await loadSession(targetSessionId);
    return;
  }

  // 如果没有任何历史会话，创建新会话
  await startNewChat();
};

onMounted(async () => {
  await initializePage();
  adjustTextareaHeight();
});
</script>

<style scoped lang="scss">
.customer-service {
  position: fixed;
  top: 64px;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--bg-color);
  padding: 1.5rem 2rem;

  .chat-container {
    max-width: 1600px;
    height: 100%;
    margin: 0 auto;
    display: flex;
    gap: 1rem;
  }

  .sidebar,
  .chat-main {
    background: rgba(255, 255, 255, 0.94);
    backdrop-filter: blur(10px);
    border-radius: 1.25rem;
  }

  .sidebar {
    width: 300px;
    display: flex;
    flex-direction: column;
    padding: 1rem;
  }

  .sidebar-header,
  .service-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 0.75rem;
  }

  .history-list {
    flex: 1;
    margin-top: 1rem;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }

  .history-item {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    width: 100%;
    padding: 0.85rem 1rem;
    border: none;
    border-radius: 0.9rem;
    background: transparent;
    color: inherit;
    text-align: left;
    position: relative;
  }

  .history-main {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 0.5rem;
    border: none;
    background: transparent;
    color: inherit;
    text-align: left;
    cursor: pointer;
    font: inherit;
  }

  .history-label {
    flex: 1;
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .pin-badge {
    flex-shrink: 0;
    padding: 0.1rem 0.45rem;
    border-radius: 999px;
    background: rgba(37, 99, 235, 0.14);
    color: #2563eb;
    font-size: 0.75rem;
  }

  .session-action {
    width: 32px;
    height: 32px;
    flex-shrink: 0;
    display: grid;
    place-items: center;
    border: none;
    border-radius: 999px;
    background: transparent;
    color: inherit;
    cursor: pointer;
  }

  .session-action:hover {
    background: rgba(15, 23, 42, 0.08);
  }

  .menu-icon {
    width: 1rem;
    height: 1rem;
  }

  .session-menu {
    position: absolute;
    top: calc(100% + 0.25rem);
    right: 0.5rem;
    z-index: 10;
    min-width: 126px;
    padding: 0.35rem;
    display: flex;
    flex-direction: column;
    gap: 0.2rem;
    border-radius: 0.85rem;
    background: white;
    box-shadow: 0 12px 32px rgba(15, 23, 42, 0.14);
  }

  .session-menu button {
    padding: 0.55rem 0.7rem;
    border: none;
    border-radius: 0.65rem;
    background: transparent;
    color: inherit;
    text-align: left;
    cursor: pointer;
  }

  .session-menu button:hover {
    background: rgba(15, 23, 42, 0.08);
  }

  .session-menu .danger-item {
    color: #dc2626;
  }

  .session-menu .danger-item:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  .session-menu .danger-item:hover:enabled {
    background: rgba(220, 38, 38, 0.08);
  }

  .history-item.active {
    background: rgba(37, 99, 235, 0.1);
    color: #2563eb;
  }

  .history-item .icon,
  .avatar,
  .send-btn .icon {
    width: 1.25rem;
    height: 1.25rem;
  }

  .chat-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    padding: 1.25rem;
    min-width: 0;
  }

  .service-info {
    display: flex;
    align-items: center;
    gap: 0.85rem;
  }

  .avatar {
    width: 2.5rem;
    height: 2.5rem;
    padding: 0.5rem;
    border-radius: 999px;
    background: rgba(37, 99, 235, 0.1);
    color: #2563eb;
  }

  .service-info p {
    color: #666;
  }

  .messages {
    flex: 1;
    overflow-y: auto;
    margin: 1rem 0;
    padding: 0 0.25rem;
  }

  .empty-state {
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #666;
    text-align: center;
    opacity: 0.3;

    .empty-icon {
      width: 8rem;
      height: 8rem;
      margin-bottom: 1.5rem;
      opacity: 0.5;
    }

    h2 {
      margin: 0;
    }
  }

  .input-area {
    display: flex;
    gap: 0.75rem;
    align-items: flex-end;
  }

  textarea {
    flex: 1;
    min-height: 56px;
    max-height: 160px;
    resize: none;
    border: 1px solid rgba(15, 23, 42, 0.08);
    border-radius: 1rem;
    padding: 1rem 1.1rem;
    outline: none;
    background: rgba(248, 250, 252, 0.95);
    color: inherit;
    font: inherit;
  }

  .primary-btn,
  .secondary-btn,
  .send-btn,
  .ticket-modal button {
    border: none;
    cursor: pointer;
    color: inherit;
  }

  .primary-btn {
    padding: 0.65rem 1rem;
    border-radius: 0.8rem;
    background: #111827;
    color: white;
  }

  .secondary-btn {
    padding: 0.65rem 1rem;
    border-radius: 0.8rem;
    background: rgba(15, 23, 42, 0.08);
  }

  .secondary-btn:disabled,
  .send-btn:disabled {
    cursor: not-allowed;
    opacity: 0.5;
  }

  .send-btn {
    width: 56px;
    height: 56px;
    border-radius: 1rem;
    background: #2563eb;
    color: white;
  }

  .ticket-modal {
    position: fixed;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(15, 23, 42, 0.42);
  }

  .modal-content {
    width: min(560px, calc(100vw - 2rem));
    padding: 1.5rem;
    border-radius: 1rem;
    background: white;
  }

  .modal-content h3 {
    margin-bottom: 1rem;
  }

  .ticket-info {
    max-height: 50vh;
    overflow-y: auto;
    margin-bottom: 1rem;
  }

  .ticket-modal button {
    width: 100%;
    padding: 0.85rem 1rem;
    border-radius: 0.8rem;
    background: #2563eb;
    color: white;
  }

  &.dark {
    .sidebar,
    .chat-main {
      background: rgba(17, 24, 39, 0.86);
    }

    .service-info p,
    .empty-state {
      color: #9ca3af;
    }

    textarea {
      background: rgba(15, 23, 42, 0.65);
      border-color: rgba(255, 255, 255, 0.08);
    }

    .secondary-btn {
      background: rgba(255, 255, 255, 0.08);
    }

    .pin-badge {
      background: rgba(96, 165, 250, 0.16);
      color: #93c5fd;
    }

    .session-action:hover,
    .session-menu button:hover {
      background: rgba(255, 255, 255, 0.08);
    }

    .session-menu {
      background: #0f172a;
      box-shadow: 0 12px 32px rgba(0, 0, 0, 0.3);
    }

    .session-menu .danger-item:hover:enabled {
      background: rgba(248, 113, 113, 0.12);
    }

    .modal-content {
      background: #111827;
    }
  }
}

@media (max-width: 960px) {
  .customer-service {
    padding: 1rem;

    .chat-container {
      flex-direction: column;
    }

    .sidebar {
      width: 100%;
      max-height: 220px;
    }
  }
}
</style>
