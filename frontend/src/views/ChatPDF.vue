<template>
  <div class="knowledge-chat" :class="{ dark: isDark }">
    <div class="layout">
      <aside class="sidebar">
        <div class="sidebar-header">
          <div>
            <h2>知识库会话</h2>
            <p>支持 PDF、Word、PPT、TXT、Markdown 等文档。</p>
          </div>
          <button class="primary-btn" @click="startNewChat">新建会话</button>
        </div>

        <div class="history-list">
          <div
            v-for="session in chatHistory"
            :key="session.sessionId"
            class="history-item"
            :class="{ active: currentSessionId === session.sessionId }"
          >
            <DocumentTextIcon class="icon" />
            <button
              class="history-main"
              @click="loadSession(session.sessionId)"
            >
              <span class="history-label">{{ session.title }}</span>
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

      <section class="workspace">
        <div class="document-panel">
          <div class="panel-header">
            <div>
              <h3>文档上传模块</h3>
              <p v-if="currentSessionId">当前会话：{{ currentSessionId }}</p>
              <p v-else>请先创建或选择会话</p>
            </div>
            <div class="panel-actions">
              <button
                class="secondary-btn"
                :disabled="isUploading || !currentSessionId"
                @click="triggerFileInput"
              >
                上传文档
              </button>
              <button
                class="secondary-btn danger"
                :disabled="isUploading || !uploadedFiles.length"
                @click="clearDocuments"
              >
                清空知识库
              </button>
            </div>
          </div>

          <div class="panel-content">
            <input
              ref="fileInputRef"
              class="hidden-input"
              type="file"
              :accept="acceptedFileTypes"
              :disabled="isUploading"
              @change="handleFileChange"
            />

            <div
              class="upload-area"
              :class="{ dragging: isDragging, disabled: !currentSessionId }"
              @dragover.prevent="handleDragOver"
              @dragleave.prevent="handleDragLeave"
              @drop.prevent="handleDrop"
              @click="!isUploading && currentSessionId && triggerFileInput()"
            >
              <div class="upload-content">
                <p v-if="isUploading" class="upload-text">
                  正在上传 {{ uploadingFileName }}...
                </p>
                <p v-else-if="!currentSessionId" class="upload-text disabled">
                  请先创建或选择会话
                </p>
                <p v-else class="upload-text">点击或拖拽文件到此处上传</p>
              </div>
            </div>

            <div v-if="uploadedFiles.length" class="uploaded-docs-section">
              <div class="section-header">
                <h4>
                  <DocumentTextIcon class="section-icon" />
                  已上传文档 ({{ uploadedFiles.length }})
                </h4>
                <span class="doc-count">点击文件可在浏览器中打开</span>
              </div>
              <div class="docs-grid">
                <div
                  v-for="fileName in uploadedFiles"
                  :key="fileName"
                  class="doc-card"
                  :class="{ active: currentDocumentName === fileName }"
                  @click="openDocumentInBrowser(fileName)"
                >
                  <DocumentTextIcon class="card-icon" />
                  <div class="doc-info">
                    <p class="doc-name" :title="fileName">{{ fileName }}</p>
                    <p class="doc-tip">点击打开</p>
                  </div>
                </div>
              </div>
            </div>

            <div v-else-if="!isUploading" class="empty-docs">
              <DocumentTextIcon class="empty-icon" />
              <p>暂无上传的文档</p>
            </div>
          </div>
        </div>

        <div class="chat-panel">
          <div class="chat-header">
            <h3>知识库问答</h3>

            <button
              class="clear-btn"
              :disabled="!currentSessionId || !currentMessages.length"
              @click="clearChatHistory"
            >
              清空对话
            </button>
          </div>

          <div ref="messagesRef" class="messages">
            <div v-if="!currentMessages.length" class="empty-chat">
              <ChatBubbleLeftRightIcon class="empty-chat-icon" />
              <p>上传文档后，开始提问吧</p>
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
              :disabled="!currentSessionId || uploadedFiles.length === 0"
              @input="adjustTextareaHeight"
              @keydown.enter.prevent="sendMessage"
            ></textarea>
            <button
              class="send-btn"
              :disabled="
                isLoading ||
                !userInput.trim() ||
                !currentSessionId ||
                uploadedFiles.length === 0
              "
              @click="sendMessage"
            >
              <PaperAirplaneIcon class="icon" />
            </button>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref } from "vue";
import { useDark } from "@vueuse/core";
import { marked } from "marked";
import DOMPurify from "dompurify";
import {
  DocumentTextIcon,
  EllipsisHorizontalIcon,
  PaperAirplaneIcon,
  ChatBubbleLeftRightIcon,
} from "@heroicons/vue/24/outline";
import { chatAPI } from "../services/api";
import {
  getActiveSessionId,
  setActiveSessionId,
  createSession,
  getSessionsByScope,
  upsertSession,
  removeSession,
} from "../utils/sessionStore";

const renderMarkdown = (content) => {
  return DOMPurify.sanitize(marked.parse(content));
};

const isDark = useDark();
const SESSION_SCOPE = "knowledge-chat";

const chatHistory = ref([]);
const currentSessionId = ref("");
const currentMessages = ref([]);
const userInput = ref("");
const isLoading = ref(false);
const messagesRef = ref(null);
const inputRef = ref(null);

const isUploading = ref(false);
const uploadingFileName = ref("");
const uploadedFiles = ref([]);
const currentDocumentName = ref("");
const fileInputRef = ref(null);
const acceptedFileTypes =
  ".pdf,.doc,.docx,.ppt,.pptx,.txt,.md,text/plain,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document";

const isDragging = ref(false);
const activeMenuSessionId = ref("");

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

const syncHistory = () => {
  chatHistory.value = getSessionsByScope(SESSION_SCOPE);
};

const startNewChat = () => {
  const session = createSession(SESSION_SCOPE);
  currentSessionId.value = session.sessionId;
  currentMessages.value = [];
  uploadedFiles.value = [];
  currentDocumentName.value = "";
  setActiveSessionId(SESSION_SCOPE, session.sessionId);
  syncHistory();
};

const loadSession = async (sessionId) => {
  currentSessionId.value = sessionId;
  setActiveSessionId(SESSION_SCOPE, sessionId);

  try {
    const [docNames, messages] = await Promise.all([
      chatAPI.listDocuments({ sessionId }),
      chatAPI.getHistoryMessages({ sessionId }),
    ]);

    uploadedFiles.value = Array.isArray(docNames) ? docNames : [];
    currentMessages.value = messages || [];
    currentDocumentName.value = "";
  } catch (error) {
    console.error("加载会话失败:", error);
    uploadedFiles.value = [];
    currentMessages.value = [];
  }

  syncHistory();
};

const toggleSessionMenu = (sessionId) => {
  activeMenuSessionId.value =
    activeMenuSessionId.value === sessionId ? "" : sessionId;
};

const renameSession = async (session) => {
  const newTitle = prompt("请输入新标题：", session.title);
  if (newTitle && newTitle.trim()) {
    upsertSession(SESSION_SCOPE, {
      sessionId: session.sessionId,
      title: newTitle.trim(),
    });
    syncHistory();
  }
};

const toggleSessionPinned = (session) => {
  upsertSession(SESSION_SCOPE, {
    sessionId: session.sessionId,
    pinned: !session.pinned,
  });
  syncHistory();
};

const deleteSessionItem = async (session) => {
  if (!confirm(`确定删除会话 "${session.title}" 吗？`)) return;

  try {
    // 先删除会话文档
    await chatAPI.clearSessionDocuments({ sessionId: session.sessionId });
  } catch (error) {
    console.error("删除文档失败:", error);
  }

  try {
    // 再删除对话历史
    await chatAPI.clearHistory({ sessionId: session.sessionId });
  } catch (error) {
    console.error("删除会话失败:", error);
  }

  removeSession(SESSION_SCOPE, session.sessionId);
  syncHistory();

  if (currentSessionId.value === session.sessionId) {
    const remaining = getSessionsByScope(SESSION_SCOPE);
    if (remaining.length > 0) {
      loadSession(remaining[0].sessionId);
    } else {
      startNewChat();
    }
  }
};

const triggerFileInput = () => {
  fileInputRef.value?.click();
};

const handleFileChange = async (e) => {
  const file = e.target.files?.[0];
  if (!file) return;
  await uploadFile(file);
  e.target.value = "";
};

const handleDragOver = () => {
  if (!isUploading.value && currentSessionId.value) {
    isDragging.value = true;
  }
};

const handleDragLeave = () => {
  isDragging.value = false;
};

const handleDrop = async (e) => {
  isDragging.value = false;
  const file = e.dataTransfer.files?.[0];
  if (
    file &&
    acceptedFileTypes.split(",").some((type) => file.name.endsWith(type.trim()))
  ) {
    await uploadFile(file);
  }
};

const uploadFile = async (file) => {
  if (!currentSessionId.value) {
    alert("请先创建或选择会话");
    return;
  }

  isUploading.value = true;
  uploadingFileName.value = file.name;

  try {
    await chatAPI.uploadDocument({
      sessionId: currentSessionId.value,
      file,
    });

    const docNames = await chatAPI.listDocuments({
      sessionId: currentSessionId.value,
    });
    uploadedFiles.value = Array.isArray(docNames) ? docNames : [];

    if (!currentDocumentName.value && uploadedFiles.value.length > 0) {
      currentDocumentName.value = uploadedFiles.value[0];
    }

    uploadingFileName.value = "";
  } catch (error) {
    console.error("上传失败:", error);
    alert(`上传失败：${error.message}`);
    uploadingFileName.value = "";
  } finally {
    isUploading.value = false;
  }
};

const openDocumentInBrowser = async (fileName) => {
  if (!currentSessionId.value) return;

  try {
    const file = await chatAPI.previewDocument({
      sessionId: currentSessionId.value,
      fileName,
    });

    if (file) {
      const url = URL.createObjectURL(file);
      window.open(url, "_blank");
      setTimeout(() => URL.revokeObjectURL(url), 60000);
    } else {
      alert("无法打开文档，请稍后重试");
    }
  } catch (error) {
    console.error("打开文档失败:", error);
    alert(`打开文档失败：${error.message}`);
  }
};

const clearDocuments = async () => {
  if (!currentSessionId.value) {
    alert("会话未初始化");
    return;
  }

  try {
    await chatAPI.clearSessionDocuments({ sessionId: currentSessionId.value });
    uploadedFiles.value = [];
    currentDocumentName.value = "";
  } catch (error) {
    console.error("清空文档失败:", error);
    alert(`清空失败：${error.message}`);
  }
};

const scrollToBottom = async () => {
  await nextTick();
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight;
  }
};

const sendMessage = async () => {
  const prompt = userInput.value.trim();
  if (!prompt || isLoading.value || !currentSessionId.value) return;

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
  });

  const assistantIndex = currentMessages.value.length - 1;
  isLoading.value = true;
  await scrollToBottom();

  try {
    const reader = await chatAPI.streamRagChat({
      prompt,
      sessionId: currentSessionId.value,
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
      };
      await scrollToBottom();
    }
  } catch (error) {
    currentMessages.value[assistantIndex] = {
      role: "assistant",
      content: error.message || "请求失败，请稍后重试。",
      timestamp: new Date(),
    };
  } finally {
    isLoading.value = false;
    await scrollToBottom();
  }
};

const clearChatHistory = async () => {
  if (!currentSessionId.value) {
    alert("会话未初始化");
    return;
  }

  try {
    await chatAPI.clearHistory({ sessionId: currentSessionId.value });
    currentMessages.value = [];
  } catch (error) {
    console.error("清空对话失败:", error);
    alert(`清空失败：${error.message}`);
  }
};

const adjustTextareaHeight = () => {
  if (!inputRef.value) return;
  inputRef.value.style.height = "auto";
  inputRef.value.style.height =
    Math.min(inputRef.value.scrollHeight, 150) + "px";
};

onMounted(async () => {
  adjustTextareaHeight();
  const existing = getActiveSessionId(SESSION_SCOPE);
  if (existing) {
    await loadSession(existing);
  } else {
    startNewChat();
  }
});
</script>

<style scoped lang="scss">
.knowledge-chat {
  position: fixed;
  top: 64px;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 1.5rem 2rem;
  background: var(--bg-color);

  .layout {
    max-width: 1600px;
    height: 100%;
    margin: 0 auto;
    display: grid;
    grid-template-columns: 280px 1fr;
    gap: 1rem;
  }

  .sidebar {
    padding: 1rem;
    background: rgba(255, 255, 255, 0.94);
    backdrop-filter: blur(10px);
    border-radius: 1.25rem;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
    display: flex;
    flex-direction: column;

    .sidebar-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1rem;

      h2 {
        margin: 0;
        font-size: 1.2rem;
      }

      p {
        margin: 0.35rem 0 0;
        font-size: 0.8rem;
        opacity: 0.7;
      }

      .primary-btn {
        background: #9333ea;
        color: white;
        border: none;
        padding: 0.6rem 1rem;
        border-radius: 0.75rem;
        cursor: pointer;
        font-weight: 500;
        transition: background-color 0.2s;

        &:hover {
          background: #7c3aed;
        }
      }
    }

    .history-list {
      flex: 1;
      overflow-y: auto;
      display: flex;
      flex-direction: column;
      gap: 0.5rem;

      .history-item {
        display: flex;
        align-items: center;
        gap: 0.5rem;
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
          background: rgba(147, 51, 234, 0.12);
          color: #9333ea;
        }

        .icon {
          width: 1.25rem;
          height: 1.25rem;
          flex-shrink: 0;
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
            background: rgba(147, 51, 234, 0.14);
            color: #9333ea;
            font-size: 0.75rem;
          }
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

          &:hover {
            background: rgba(15, 23, 42, 0.08);
          }

          .menu-icon {
            width: 1rem;
            height: 1rem;
          }
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

          button {
            padding: 0.55rem 0.7rem;
            border: none;
            border-radius: 0.65rem;
            background: transparent;
            color: inherit;
            text-align: left;
            cursor: pointer;

            &:hover {
              background: rgba(15, 23, 42, 0.08);
            }

            &.danger-item {
              color: #dc2626;

              &:disabled {
                opacity: 0.5;
                cursor: not-allowed;
              }

              &:hover:enabled {
                background: rgba(220, 38, 38, 0.08);
              }
            }
          }
        }
      }
    }
  }

  .workspace {
    flex: 1;
    min-width: 0;
    min-height: 0;
    display: grid;
    grid-template-columns: minmax(400px, 1fr) minmax(500px, 1fr);
    gap: 1rem;
  }

  .document-panel {
    display: flex;
    flex-direction: column;
    min-width: 0;
    height: 100%;
    padding: 1.5rem;
    background: rgba(255, 255, 255, 0.94);
    backdrop-filter: blur(10px);
    border-radius: 1.25rem;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);

    .panel-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 1rem;
      flex-shrink: 0;

      h3 {
        margin: 0;
        font-size: 1.2rem;
      }

      p {
        margin: 0.35rem 0 0;
        font-size: 0.85rem;
        opacity: 0.7;
      }

      .panel-actions {
        display: flex;
        gap: 0.5rem;
        flex-shrink: 0;

        .secondary-btn {
          padding: 0.55rem 0.9rem;
          border: none;
          border-radius: 0.7rem;
          background: rgba(147, 51, 234, 0.12);
          color: #9333ea;
          cursor: pointer;
          font-weight: 500;
          transition: all 0.2s;
          font-size: 0.9rem;

          &:hover:not(:disabled) {
            background: rgba(147, 51, 234, 0.2);
          }

          &:disabled {
            opacity: 0.5;
            cursor: not-allowed;
          }

          &.danger {
            background: rgba(239, 68, 68, 0.12);
            color: #ef4444;

            &:hover:not(:disabled) {
              background: rgba(239, 68, 68, 0.2);
            }
          }
        }
      }
    }

    .panel-content {
      flex: 1;
      min-height: 0;
      overflow-y: auto;
      display: flex;
      flex-direction: column;
    }

    .upload-area {
      flex: 0 0 auto;
      min-height: 120px;
      max-height: 150px;
      border: 2px dashed rgba(147, 51, 234, 0.3);
      border-radius: 1rem;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: all 0.3s;
      background: rgba(147, 51, 234, 0.04);

      &:hover:not(.disabled) {
        border-color: rgba(147, 51, 234, 0.6);
        background: rgba(147, 51, 234, 0.08);
      }

      &.dragging {
        border-color: #9333ea;
        background: rgba(147, 51, 234, 0.12);
      }

      &.disabled {
        cursor: not-allowed;
        opacity: 0.6;
      }

      .upload-content {
        text-align: center;

        .upload-text {
          margin: 0;
          font-size: 0.95rem;
          color: inherit;

          &.disabled {
            color: #9ca3af;
          }
        }
      }
    }

    .uploaded-docs-section {
      margin-top: 1rem;

      .section-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 0.75rem;

        h4 {
          margin: 0;
          display: flex;
          align-items: center;
          gap: 0.5rem;
          font-size: 0.95rem;

          .section-icon {
            width: 1.15rem;
            height: 1.15rem;
            color: #9333ea;
          }
        }

        .doc-count {
          font-size: 0.8rem;
          opacity: 0.7;
        }
      }

      .docs-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
        gap: 0.65rem;
        max-height: 300px;
        overflow-y: auto;
      }

      .doc-card {
        display: flex;
        align-items: center;
        gap: 0.75rem;
        padding: 0.85rem;
        border: 1px solid rgba(147, 51, 234, 0.15);
        border-radius: 0.75rem;
        background: rgba(147, 51, 234, 0.06);
        cursor: pointer;
        transition: all 0.2s;

        &:hover {
          background: rgba(147, 51, 234, 0.12);
          border-color: rgba(147, 51, 234, 0.3);
        }

        &.active {
          background: rgba(147, 51, 234, 0.18);
          border-color: rgba(147, 51, 234, 0.4);
          color: #9333ea;
        }

        .card-icon {
          width: 1.5rem;
          height: 1.5rem;
          color: #9333ea;
          flex-shrink: 0;
        }

        .doc-info {
          flex: 1;
          min-width: 0;

          .doc-name {
            margin: 0;
            font-size: 0.9rem;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .doc-tip {
            margin: 0.25rem 0 0;
            font-size: 0.75rem;
            opacity: 0.7;
          }
        }
      }
    }

    .empty-docs {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      min-height: 200px;
      opacity: 0.5;

      .empty-icon {
        width: 4rem;
        height: 4rem;
        margin-bottom: 1rem;
      }

      p {
        margin: 0;
      }
    }
  }

  .chat-panel {
    display: flex;
    flex-direction: column;
    min-width: 0;
    height: 100%;
    padding: 1.5rem;
    background: rgba(255, 255, 255, 0.94);
    backdrop-filter: blur(10px);
    border-radius: 1.25rem;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);

    .chat-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1rem;

      h3 {
        margin: 0;
        font-size: 1.2rem;
      }

      p {
        margin: 0.35rem 0 0;
        font-size: 0.85rem;
        opacity: 0.7;
      }

      .clear-btn {
        padding: 0.5rem 0.85rem;
        border: none;
        border-radius: 0.65rem;
        background: rgba(239, 68, 68, 0.12);
        color: #ef4444;
        cursor: pointer;
        font-size: 0.85rem;

        &:hover:not(:disabled) {
          background: rgba(239, 68, 68, 0.2);
        }

        &:disabled {
          opacity: 0.5;
          cursor: not-allowed;
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
      max-height: calc(100vh - 250px);

      .empty-chat {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        opacity: 0.3;
        position: relative;

        .empty-chat-icon {
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

        &:disabled {
          background: #f5f5f5;
          cursor: not-allowed;
        }
      }

      .send-btn {
        background: #007cf0;
        color: white;
        border: none;
        border-radius: 0.5rem;
        width: 2.5rem;
        height: 2.5rem;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: background-color 0.2s;

        &:hover:not(:disabled) {
          background: #0066cc;
        }

        &:disabled {
          opacity: 0.5;
          cursor: not-allowed;
        }

        .icon {
          width: 1.25rem;
          height: 1.25rem;
        }
      }
    }
  }
}

@media (max-width: 1200px) {
  .knowledge-chat {
    padding: 1rem;

    .layout {
      grid-template-columns: 1fr;
    }

    .workspace {
      grid-template-columns: 1fr;
    }
  }
}

.dark {
  background: linear-gradient(135deg, #020617 0%, #0f172a 100%);

  .sidebar {
    background: rgba(30, 41, 59, 0.94);
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.3);

    .sidebar-header {
      h2 {
        color: #f1f5f9;
      }

      p {
        color: #94a3b8;
      }

      .primary-btn {
        background: #8b5cf6;
        color: white;

        &:hover {
          background: #7c3aed;
        }
      }
    }

    .history-list {
      .history-item {
        background: rgba(51, 65, 85, 0.6);
        color: #e2e8f0;

        &:hover {
          background: rgba(51, 65, 85, 0.85);
        }

        &.active {
          background: rgba(139, 92, 246, 0.2);
          color: #a78bfa;
        }

        .history-main {
          .history-label {
            color: #e2e8f0;
          }

          .pin-badge {
            background: rgba(139, 92, 246, 0.2);
            color: #a78bfa;
          }
        }

        .session-action {
          color: #e2e8f0;

          &:hover {
            background: rgba(255, 255, 255, 0.08);
          }
        }

        .session-menu {
          background: #1e293b;
          box-shadow: 0 12px 32px rgba(0, 0, 0, 0.4);

          button {
            color: #e2e8f0;

            &:hover {
              background: rgba(255, 255, 255, 0.08);
            }

            &.danger-item {
              color: #f87171;

              &:hover:enabled {
                background: rgba(248, 113, 113, 0.1);
              }
            }
          }
        }
      }
    }
  }

  .workspace {
    .document-panel {
      background: rgba(30, 41, 59, 0.94);
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.3);

      .panel-header {
        h3 {
          color: #f1f5f9;
        }

        p {
          color: #94a3b8;
        }

        .panel-actions {
          .secondary-btn {
            background: rgba(139, 92, 246, 0.2);
            color: #a78bfa;

            &:hover:not(:disabled) {
              background: rgba(139, 92, 246, 0.3);
            }

            &.danger {
              background: rgba(248, 113, 113, 0.2);
              color: #f87171;

              &:hover:not(:disabled) {
                background: rgba(248, 113, 113, 0.3);
              }
            }
          }
        }
      }

      .upload-area {
        border-color: rgba(139, 92, 246, 0.4);
        background: rgba(139, 92, 246, 0.08);

        &:hover:not(.disabled) {
          border-color: rgba(139, 92, 246, 0.7);
          background: rgba(139, 92, 246, 0.12);
        }

        &.dragging {
          border-color: #a78bfa;
          background: rgba(139, 92, 246, 0.16);
        }

        .upload-content {
          .upload-text {
            color: #e2e8f0;

            &.disabled {
              color: #64748b;
            }
          }
        }
      }

      .uploaded-docs-section {
        .section-header {
          h4 {
            color: #f1f5f9;

            .section-icon {
              color: #a78bfa;
            }
          }

          .doc-count {
            color: #94a3b8;
          }
        }

        .docs-grid {
          .doc-card {
            border-color: rgba(139, 92, 246, 0.3);
            background: rgba(139, 92, 246, 0.1);
            color: #e2e8f0;

            &:hover {
              background: rgba(139, 92, 246, 0.2);
              border-color: rgba(139, 92, 246, 0.5);
            }

            &.active {
              background: rgba(139, 92, 246, 0.25);
              border-color: rgba(139, 92, 246, 0.6);
              color: #c4b5fd;
            }

            .card-icon {
              color: #a78bfa;
            }

            .doc-info {
              .doc-name {
                color: #e2e8f0;
              }

              .doc-tip {
                color: #94a3b8;
              }
            }
          }
        }
      }

      .empty-docs {
        color: #94a3b8;

        .empty-icon {
          color: #64748b;
        }
      }
    }

    .chat-panel {
      background: rgba(30, 41, 59, 0.94);
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.3);

      .chat-header {
        h3 {
          color: #f1f5f9;
        }

        p {
          color: #94a3b8;
        }

        .clear-btn {
          background: rgba(248, 113, 113, 0.2);
          color: #f87171;

          &:hover:not(:disabled) {
            background: rgba(248, 113, 113, 0.3);
          }
        }
      }

      .messages {
        background: rgba(30, 41, 59, 0.5);

        .empty-chat {
          color: #94a3b8;

          .empty-chat-icon {
            color: #64748b;
          }

          p {
            color: #94a3b8;
          }
        }

        .message-wrapper {
          &.message-user {
            .message-content {
              background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
              color: white;
            }

            .message-time {
              color: #94a3b8;
            }
          }

          &.message-assistant {
            .message-content {
              background: linear-gradient(135deg, #334155 0%, #475569 100%);
              color: #f1f5f9;

              .markdown-content {
                h1,
                h2,
                h3,
                h4,
                h5,
                h6 {
                  color: #f1f5f9;
                }

                p {
                  color: #e2e8f0;
                }

                code {
                  background: rgba(0, 0, 0, 0.3);
                  color: #e2e8f0;
                }

                pre {
                  background: #0f172a;

                  code {
                    color: #cbd5e1;
                  }
                }

                blockquote {
                  border-left-color: rgba(148, 163, 184, 0.3);
                  color: #94a3b8;
                }

                table {
                  th,
                  td {
                    border-color: rgba(148, 163, 184, 0.2);
                  }

                  th {
                    background: rgba(51, 65, 85, 0.5);
                  }
                }

                a {
                  color: #38bdf8;

                  &:hover {
                    color: #0ea5e9;
                  }
                }
              }
            }

            .message-time {
              color: #94a3b8;
            }
          }
        }
      }

      .input-area {
        textarea {
          border-color: rgba(148, 163, 184, 0.2);
          background: #1e293b;
          color: #f1f5f9;

          &:focus {
            border-color: #38bdf8;
            box-shadow: 0 0 0 2px rgba(56, 189, 248, 0.1);
          }

          &:disabled {
            background: #0f172a;
          }
        }

        .send-btn {
          background: #0ea5e9;
          color: white;

          &:hover:not(:disabled) {
            background: #0284c7;
          }
        }
      }
    }
  }
}
</style>
