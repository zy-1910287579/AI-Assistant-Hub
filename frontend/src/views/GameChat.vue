<template>
  <div class="game-chat" :class="{ dark: isDark }">
    <div class="game-container">
      <!-- 游戏开始界面 -->
      <div v-if="!isGameStarted" class="game-start">
        <h2>初恋那些事</h2>
        <div class="input-area">
          <textarea
            v-model="angerReason"
            placeholder="请输入当年她离开的原因（可选）..."
            rows="3"
          ></textarea>
          <button class="start-button" @click="startGame">开始游戏</button>
        </div>
      </div>

      <!-- 聊天界面 -->
      <div v-else class="chat-main">
        <!-- 游戏统计信息 -->
        <div class="game-stats">
          <div class="stat-item">
            <span class="label">
              <HeartIcon
                class="heart-icon"
                :class="{ beating: forgiveness >= 100 }"
              />
              女友原谅值
            </span>
            <div class="progress-bar">
              <div
                class="progress"
                :style="{ width: `${forgiveness}%` }"
                :class="{
                  low: forgiveness < 30,
                  medium: forgiveness >= 30 && forgiveness < 70,
                  high: forgiveness >= 70,
                }"
              ></div>
            </div>
            <span class="value">{{ forgiveness }}%</span>
          </div>
          <div class="stat-item">
            <span class="label">对话轮次</span>
            <span class="value">{{ currentRound }}/{{ MAX_ROUNDS }}</span>
          </div>
        </div>

        <div class="messages" ref="messagesRef">
          <div v-if="!currentMessages.length" class="empty-state">
            <HeartIcon class="empty-icon" />
            <p>开始初恋的对话吧</p>
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
            v-model="userInput"
            @keydown.enter.prevent="sendMessage()"
            placeholder="输入消息..."
            rows="1"
            ref="inputRef"
            :disabled="isGameOver"
          ></textarea>
          <button
            class="send-button"
            @click="sendMessage()"
            :disabled="isStreaming || !userInput.trim() || isGameOver"
          >
            <PaperAirplaneIcon class="icon" />
          </button>
        </div>
      </div>

      <!-- 游戏结束提示 -->
      <div
        v-if="isGameOver"
        class="game-over"
        :class="{ success: forgiveness >= 100 }"
      >
        <div class="result">{{ gameResult }}</div>
        <button class="restart-button" @click="resetGame">重新开始</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from "vue";
import { useDark } from "@vueuse/core";
import { marked } from "marked";
import DOMPurify from "dompurify";
import { PaperAirplaneIcon, HeartIcon } from "@heroicons/vue/24/outline";
import { onBeforeRouteLeave } from "vue-router";
import ChatMessage from "../components/ChatMessage.vue";
import { chatAPI } from "../services/api";
import {
  createSession,
  getActiveSessionId,
  getStoredUserId,
  removeSession,
} from "../utils/sessionStore";

const renderMarkdown = (content) => {
  return DOMPurify.sanitize(marked.parse(content));
};

const SESSION_SCOPE = "game";

const isDark = useDark();
const messagesRef = ref(null);
const inputRef = ref(null);
const userInput = ref("");
const isStreaming = ref(false);
const currentSessionId = ref("");
const currentMessages = ref([]);
const angerReason = ref("");
const isGameStarted = ref(false);
const isGameOver = ref(false);
const gameResult = ref("");
const MAX_ROUNDS = 10;
const currentRound = ref(0);
const forgiveness = ref(0);
let activeRequestController = null;
let isCleaningUpSession = false;

// 自动调整输入框高度
const adjustTextareaHeight = () => {
  const textarea = inputRef.value;
  if (textarea) {
    textarea.style.height = "auto";
    textarea.style.height = textarea.scrollHeight + "px";
  }
};

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick();
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight;
  }
};

const ensureSession = () => {
  if (currentSessionId.value) {
    return currentSessionId.value;
  }

  const session = createSession(SESSION_SCOPE);
  currentSessionId.value = session.sessionId;
  return session.sessionId;
};

const resetLocalGameState = () => {
  isGameStarted.value = false;
  isGameOver.value = false;
  gameResult.value = "";
  currentMessages.value = [];
  angerReason.value = "";
  userInput.value = "";
  currentSessionId.value = "";
  currentRound.value = 0;
  forgiveness.value = 0;
};

const abortActiveRequest = () => {
  activeRequestController?.abort();
  activeRequestController = null;
  isStreaming.value = false;
};

const cleanupCurrentSession = async () => {
  const sessionId = currentSessionId.value;

  if (!sessionId || isCleaningUpSession) {
    return;
  }

  isCleaningUpSession = true;
  abortActiveRequest();

  try {
    await chatAPI.clearHistory({
      userId: getStoredUserId(),
      sessionId,
    });
  } catch (error) {
    console.error("清理游戏历史失败:", error);
  } finally {
    removeSession(SESSION_SCOPE, sessionId);
    isCleaningUpSession = false;
  }
};

// 开始游戏
const startGame = async () => {
  isGameStarted.value = true;
  isGameOver.value = false;
  gameResult.value = "";
  const session = createSession(SESSION_SCOPE);
  currentSessionId.value = session.sessionId;
  currentMessages.value = [];
  currentRound.value = 0;
  forgiveness.value = 0;

  const startPrompt = angerReason.value
    ? `开始游戏，女友分手原因：${angerReason.value}`
    : "随机生成分手原因，并开始游戏";

  await sendMessage(startPrompt);
};

// 重置游戏
const resetGame = () => {
  cleanupCurrentSession().finally(() => {
    resetLocalGameState();
  });
};

// 发送消息
const sendMessage = async (content) => {
  if (isStreaming.value || (!content && !userInput.value.trim())) return;

  const sessionId = ensureSession();
  const messageContent = content || userInput.value.trim();

  const userMessage = {
    role: "user",
    content: messageContent,
    timestamp: new Date(),
  };
  currentMessages.value.push(userMessage);

  if (!content) {
    userInput.value = "";
    adjustTextareaHeight();
    currentRound.value++;
  }
  await scrollToBottom();

  const assistantMessage = {
    role: "assistant",
    content: "",
    timestamp: new Date(),
  };
  currentMessages.value.push(assistantMessage);
  isStreaming.value = true;

  let accumulatedContent = "";

  try {
    const requestController = new AbortController();
    activeRequestController = requestController;
    const reader = await chatAPI.sendGameMessage({
      prompt: messageContent,
      userId: getStoredUserId(),
      sessionId,
      signal: requestController.signal,
    });
    const decoder = new TextDecoder("utf-8");

    while (true) {
      try {
        const { value, done } = await reader.read();
        if (done) break;

        accumulatedContent += decoder.decode(value);

        const forgivenessMatch =
          accumulatedContent.match(/原谅值[:：]\s*(\d+)/i);
        if (forgivenessMatch) {
          const newForgiveness = parseInt(forgivenessMatch[1]);
          if (!isNaN(newForgiveness)) {
            forgiveness.value = Math.min(100, Math.max(0, newForgiveness));

            if (forgiveness.value >= 100) {
              isGameOver.value = true;
              gameResult.value =
                "你们都很好,在另一个时空,你们其实一直在一起！💕";
            } else if (forgiveness.value <= 0) {
              isGameOver.value = true;
              gameResult.value = "游戏失败：但你的人生远不止这些,加油!少年 👋";
              inputDisabled.value = true;
            }
          }
        }
        await nextTick(() => {
          const updatedMessage = {
            ...assistantMessage,
            content: accumulatedContent,
          };
          const lastIndex = currentMessages.value.length - 1;
          currentMessages.value.splice(lastIndex, 1, updatedMessage);
        });
        await scrollToBottom();
      } catch (readError) {
        console.error("读取流错误:", readError);
        break;
      }
    }

    if (currentRound.value >= MAX_ROUNDS) {
      isGameOver.value = true;
      if (forgiveness.value >= 100) {
        gameResult.value = "恭喜你！在最后一轮成功哄好了女友！💕";
      } else {
        gameResult.value = `游戏结束：对话轮次已达上限(${MAX_ROUNDS}轮)，当前原谅值为${forgiveness.value}，很遗憾没能完全哄好女友`;
      }
    } else if (accumulatedContent.includes("游戏结束")) {
      isGameOver.value = true;
      gameResult.value = accumulatedContent;
    }
  } catch (error) {
    if (error?.name === "AbortError") {
      return;
    }

    console.error("发送消息失败:", error);
    assistantMessage.content = "抱歉，发生了错误，请稍后重试。";
  } finally {
    activeRequestController = null;
    isStreaming.value = false;
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

onBeforeRouteLeave(async () => {
  await cleanupCurrentSession();
  resetLocalGameState();
});

onMounted(async () => {
  currentSessionId.value = getActiveSessionId(SESSION_SCOPE);
  adjustTextareaHeight();

  if (currentSessionId.value) {
    try {
      const history = await chatAPI.getHistoryMessages({
        sessionId: currentSessionId.value,
      });
      if (history && history.length > 0) {
        currentMessages.value = history;
        isGameStarted.value = true;
        await scrollToBottom();
      }
    } catch (error) {
      console.error("加载游戏历史失败:", error);
    }
  }
});
</script>

<style scoped lang="scss">
.game-chat {
  position: fixed;
  top: 64px;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  overflow: hidden;
  z-index: 1;

  .game-container {
    flex: 1;
    display: flex;
    flex-direction: column;
    max-width: 1200px;
    width: 100%;
    margin: 0 auto;
    padding: 1.5rem 2rem;
    position: relative;
    height: 100%;
  }

  .game-start {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 2rem;
    padding: 3rem;
    background: rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(10px);
    border-radius: 2rem;
    box-shadow: 0 8px 30px rgba(236, 72, 153, 0.2);
    margin: 2rem;
    animation: fadeIn 0.5s ease-out;

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

    h2 {
      font-size: 2.5rem;
      background: linear-gradient(135deg, #ec4899 0%, #a855f7 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      margin: 0;
      font-weight: 700;
      text-shadow: 0 2px 10px rgba(236, 72, 153, 0.3);
    }

    .input-area {
      width: 100%;
      max-width: 600px;
      display: flex;
      flex-direction: column;
      gap: 1rem;

      textarea {
        width: 100%;
        padding: 1rem;
        border: 1px solid rgba(0, 0, 0, 0.1);
        border-radius: 0.5rem;
        resize: none;
        font-family: inherit;
        font-size: 1rem;
        line-height: 1.5;

        &:focus {
          outline: none;
          border-color: #007cf0;
          box-shadow: 0 0 0 2px rgba(0, 124, 240, 0.1);
        }
      }

      .start-button {
        padding: 1rem 2rem;
        background: #007cf0;
        color: white;
        border: none;
        border-radius: 0.5rem;
        font-size: 1.1rem;
        cursor: pointer;
        transition: background-color 0.3s;

        &:hover {
          background: #0066cc;
        }
      }
    }
  }

  .chat-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10px);
    border-radius: 1rem;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
    overflow: hidden;

    .game-stats {
      position: sticky;
      top: 0;
      background: linear-gradient(
        135deg,
        rgba(236, 72, 153, 0.9),
        rgba(167, 139, 250, 0.9)
      );
      color: white;
      padding: 1rem;
      z-index: 10;
      backdrop-filter: blur(10px);
      display: flex;
      gap: 2rem;
      justify-content: center;
      align-items: center;
      margin-bottom: 1rem;
      border-radius: 1rem;
      box-shadow: 0 4px 15px rgba(236, 72, 153, 0.3);

      .stat-item {
        display: flex;
        align-items: center;
        gap: 0.75rem;

        .label {
          display: flex;
          align-items: center;
          gap: 0.5rem;
          font-size: 1rem;
          font-weight: 500;
          color: white;
          text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);

          .heart-icon {
            width: 1.5rem;
            height: 1.5rem;
            color: #f472b6;
            animation: heartbeat 1.5s ease-in-out infinite;

            &.beating {
              color: #ec4899;
              filter: drop-shadow(0 0 10px rgba(236, 72, 153, 0.8));
            }
          }
        }

        .progress-bar {
          width: 150px;
          height: 1.25rem;
          background: rgba(255, 255, 255, 0.3);
          border-radius: 1rem;
          overflow: hidden;
          box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.1);

          .progress {
            height: 100%;
            background: linear-gradient(90deg, #f472b6 0%, #ec4899 100%);
            border-radius: 1rem;
            transition: width 0.5s ease;
            box-shadow: 0 0 10px rgba(236, 72, 153, 0.5);

            &.low {
              background: linear-gradient(90deg, #f87171 0%, #ef4444 100%);
              box-shadow: 0 0 10px rgba(239, 68, 68, 0.5);
            }

            &.medium {
              background: linear-gradient(90deg, #fbbf24 0%, #f59e0b 100%);
              box-shadow: 0 0 10px rgba(245, 158, 11, 0.5);
            }

            &.high {
              background: linear-gradient(90deg, #34d399 0%, #10b981 100%);
              box-shadow: 0 0 10px rgba(16, 185, 129, 0.5);
            }
          }
        }

        .value {
          min-width: 3rem;
          text-align: center;
          font-size: 1.1rem;
          font-weight: 600;
          color: white;
          text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
        }
      }
    }

    .messages {
      flex: 1;
      overflow-y: auto;
      padding: 2rem;
      background: rgba(255, 255, 255, 0.5);
      backdrop-filter: blur(5px);
      border-radius: 1.5rem;
      margin: 1rem;
      box-shadow: inset 0 2px 10px rgba(236, 72, 153, 0.1);

      .empty-state {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        opacity: 0.4;

        .empty-icon {
          width: 8rem;
          height: 8rem;
          margin-bottom: 1.5rem;
          color: #ec4899;
          animation: heartbeat 2s ease-in-out infinite;
        }

        @keyframes heartbeat {
          0%,
          100% {
            transform: scale(1);
          }
          50% {
            transform: scale(1.1);
          }
        }

        p {
          margin: 0;
          font-size: 1.2rem;
          color: #ec4899;
          font-weight: 500;
        }
      }

      .message-wrapper {
        display: flex;
        margin-bottom: 1.5rem;
        padding: 0 0.5rem;
        animation: messageSlideIn 0.3s ease-out;

        @keyframes messageSlideIn {
          from {
            opacity: 0;
            transform: translateY(10px);
          }
          to {
            opacity: 1;
            transform: translateY(0);
          }
        }

        &.message-user {
          justify-content: flex-end;
          flex-direction: column;
          align-items: flex-end;

          .message-content {
            background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
            color: white;
            max-width: 70%;
            padding: 1rem 1.25rem;
            border-radius: 1.25rem 1.25rem 0 1.25rem;
            word-break: break-word;
            box-shadow: 0 2px 8px rgba(14, 165, 233, 0.2);
            position: relative;

            .message-text {
              margin: 0;
              line-height: 1.6;
              white-space: pre-wrap;
            }
          }

          .message-time {
            margin-top: 0.25rem;
            font-size: 0.75rem;
            color: #64748b;
          }
        }

        &.message-assistant {
          justify-content: flex-start;
          flex-direction: column;
          align-items: flex-start;

          .message-content {
            background: linear-gradient(135deg, #ffffff 0%, #f0f9ff 100%);
            color: #0f172a;
            max-width: 70%;
            padding: 1rem 1.25rem;
            border-radius: 1.25rem 1.25rem 1.25rem 0;
            word-break: break-word;
            box-shadow: 0 2px 8px rgba(14, 165, 233, 0.15);
            position: relative;

            .message-text {
              margin: 0;
              line-height: 1.6;
              white-space: pre-wrap;
            }
          }

          .message-time {
            margin-top: 0.25rem;
            font-size: 0.75rem;
            color: #64748b;
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

    .input-area {
      flex-shrink: 0;
      padding: 1.5rem 2rem;
      background: linear-gradient(
        135deg,
        rgba(255, 255, 255, 0.95) 0%,
        #fef3c7 100%
      );
      border-top: 2px solid rgba(236, 72, 153, 0.2);
      display: flex;
      gap: 1rem;
      align-items: flex-end;
      backdrop-filter: blur(10px);

      textarea {
        flex: 1;
        resize: none;
        border: 2px solid rgba(236, 72, 153, 0.2);
        background: white;
        border-radius: 1.5rem;
        padding: 1rem 1.5rem;
        color: inherit;
        font-family: inherit;
        font-size: 1rem;
        line-height: 1.5;
        max-height: 150px;
        transition: all 0.3s ease;

        &:focus {
          outline: none;
          border-color: #ec4899;
          box-shadow: 0 0 0 3px rgba(236, 72, 153, 0.15);
        }

        &::placeholder {
          color: #d1d5db;
        }
      }

      .send-button {
        flex-shrink: 0;
        width: 3.5rem;
        height: 3.5rem;
        border: none;
        border-radius: 50%;
        background: linear-gradient(135deg, #f472b6 0%, #ec4899 100%);
        color: white;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all 0.3s ease;
        box-shadow: 0 4px 15px rgba(236, 72, 153, 0.3);

        &:hover:not(:disabled) {
          transform: scale(1.1) rotate(15deg);
          box-shadow: 0 6px 20px rgba(236, 72, 153, 0.5);
        }

        &:active:not(:disabled) {
          transform: scale(0.95);
        }

        &:disabled {
          opacity: 0.5;
          cursor: not-allowed;
        }

        .icon {
          width: 1.5rem;
          height: 1.5rem;
        }
      }
    }
  }

  .game-over {
    position: absolute;
    bottom: 6rem;
    left: 50%;
    transform: translateX(-50%);
    background: rgba(0, 0, 0, 0.8);
    color: white;
    padding: 1rem 2rem;
    border-radius: 0.5rem;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 1rem;

    .result {
      font-size: 1.1rem;
    }

    .restart-button {
      padding: 0.5rem 1rem;
      background: #007cf0;
      color: white;
      border: none;
      border-radius: 0.25rem;
      cursor: pointer;
      transition: background-color 0.3s;

      &:hover {
        background: #0066cc;
      }
    }

    &.success {
      background: rgba(82, 196, 26, 0.9);

      .restart-button {
        background: #52c41a;

        &:hover {
          background: #389e0d;
        }
      }
    }
  }
}

.dark {
  background: linear-gradient(135deg, #0c1445 0%, #1a1f3a 100%);
  color: #60a5fa;

  .game-start {
    h2 {
      color: #93c5fd;
    }

    .input-area {
      textarea {
        background: rgba(255, 255, 255, 0.05);
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

  .chat-main {
    background: rgba(40, 40, 40, 0.95);
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.2);

    .game-stats {
      background: rgba(0, 0, 0, 0.3);
      color: #60a5fa;

      .stat-item {
        .label {
          color: #93c5fd;
        }

        .value {
          color: #ffffff;
        }
      }
    }

    .messages {
      color: #60a5fa;
    }

    .input-area {
      background: rgba(30, 30, 30, 0.98);
      border-top: 1px solid rgba(255, 255, 255, 0.05);

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

        &:disabled {
          background: rgba(30, 30, 30, 0.95);
          color: rgba(96, 165, 250, 0.3);
        }
      }

      .send-button {
        &:disabled {
          background: rgba(255, 255, 255, 0.1);
          color: rgba(255, 255, 255, 0.3);
        }
      }
    }
  }

  .game-over {
    color: #ffffff;

    .result {
      color: #ffffff;
    }
  }
}

@keyframes heartbeat {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.2);
  }
}
</style>
