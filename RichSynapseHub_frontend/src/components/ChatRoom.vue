<template>
  <div class="chat-container">
    <!-- 聊天记录区域 -->
    <div class="chat-messages" ref="messagesContainer">
      <div v-for="(msg, index) in messages" :key="index" class="message-wrapper">
        <!-- AI消息 -->
        <div v-if="!msg.isUser"
             class="message ai-message"
             :class="[msg.type]">
          <div class="avatar ai-avatar"></div>
          <div class="message-bubble">
            <div class="message-content">
              <span v-html="getParsedContent(msg)"></span>
              <div v-if="connectionStatus === 'connecting' && index === messages.length - 1"
                   class="typing-indicator">
                <span class="dot"></span>
                <span class="dot"></span>
                <span class="dot"></span>
              </div>
            </div>
            <div class="message-time">{{ formatTime(msg.time) }}</div>
          </div>
        </div>

        <!-- 用户消息 -->
        <div v-else class="message user-message" :class="[msg.type]">
          <div class="message-bubble">
            <div class="message-content">
              <span v-html="getParsedContent(msg)"></span>
            </div>
            <div class="message-time">{{ formatTime(msg.time) }}</div>
          </div>
          <div class="avatar user-avatar">
            <div class="avatar-placeholder">我</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="chat-input-container">
      <div class="chat-input">
        <textarea
            ref="inputField"
            v-model="inputMessage"
            @keydown.enter.prevent="sendMessage"
            @input="resizeInput"
            placeholder="请输入消息..."
            class="input-box"
            :disabled="connectionStatus === 'connecting'"
        ></textarea>
        <button
            @click="sendMessage"
            class="send-button"
            :disabled="connectionStatus === 'connecting' || !inputMessage.trim()"
        >发送
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted, nextTick, watch} from 'vue'
import {marked} from 'marked'
import DOMPurify from 'dompurify'

const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  },
  connectionStatus: {
    type: String,
    default: 'disconnected'
  },
  aiType: {
    type: String,
    default: 'default'
  }
})

const emit = defineEmits(['send-message'])

const inputMessage = ref('')
const messagesContainer = ref(null)
const inputField = ref(null)
const contentCache = new Map()

// 配置Marked解析器
marked.setOptions({
  gfm: true,
  breaks: true,
  headerIds: false
})

// 发送消息
const sendMessage = () => {
  if (!inputMessage.value.trim()) return

  emit('send-message', inputMessage.value)
  inputMessage.value = ''

  if (inputField.value) {
    inputField.value.style.height = 'auto'
  }
}

// 格式化时间
const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  return date.toLocaleTimeString('zh-CN', {hour: '2-digit', minute: '2-digit'})
}

// 统一处理Markdown解析
const getParsedContent = (msg) => {
  const cacheKey = `${msg.content}_${msg.time}`;

  if (contentCache.has(cacheKey)) {
    return contentCache.get(cacheKey);
  }

  const rawHTML = marked.parse(msg.content);
  const cleanHTML = DOMPurify.sanitize(rawHTML);

  contentCache.set(cacheKey, cleanHTML);
  return cleanHTML;
}

// 自动滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 监听消息变化自动滚动
watch(() => props.messages, () => {
  contentCache.clear();
}, {deep: true});

watch(() => props.messages.length, scrollToBottom)
watch(() => props.messages.map(m => m.content).join(''), scrollToBottom)

// 调整输入框高度
const resizeInput = () => {
  if (inputField.value) {
    inputField.value.style.height = 'auto'
    inputField.value.style.height = Math.min(inputField.value.scrollHeight, 200) + 'px'
    scrollToBottom()
  }
}

onMounted(() => {
  scrollToBottom()
  if (inputField.value) {
    inputField.value.style.height = '56px'
  }
})
</script>

<style scoped lang="scss">
.chat-container {
  display: flex;
  flex-direction: column;
  height: 70vh;
  min-height: 600px;
  background-color: #f5f5f5;
  border-radius: 8px;
  overflow: hidden;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px 16px 20px;
}

.message-wrapper {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  width: 100%;
}

.message {
  display: flex;
  align-items: flex-start;
  max-width: 85%;
  margin-bottom: 8px;
}

.user-message {
  margin-left: auto;
  flex-direction: row;
}

.ai-message {
  margin-right: auto;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-avatar {
  margin-left: 8px;
}

.ai-avatar {
  margin-right: 8px;
  background: linear-gradient(135deg, #4361ee, #c8e5ff);
  position: relative;

  &::after {
    content: 'AI';
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    color: white;
    font-size: 18px;
    font-weight: bold;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  }
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #007bff;
  color: white;
  font-weight: bold;
}

.message-bubble {
  padding: 12px;
  border-radius: 18px;
  position: relative;
  word-wrap: break-word;
  min-width: 100px;
}

.user-message .message-bubble {
  background-color: #007bff;
  color: white;
  border-bottom-right-radius: 4px;
  text-align: left;
}

.ai-message .message-bubble {
  background-color: #e9e9eb;
  color: #333;
  border-bottom-left-radius: 4px;
  text-align: left;
}

.message-content {
  font-size: 16px;
  line-height: 1.5;
  white-space: pre-wrap;
}

.message-time {
  font-size: 12px;
  opacity: 0.7;
  margin-top: 4px;
  text-align: right;
}

.chat-input-container {
  padding: 16px;
  background-color: white;
  border-top: 1px solid #e0e0e0;
  z-index: 100;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
}

.chat-input {
  display: flex;
  align-items: flex-end;
}

.input-box {
  flex-grow: 1;
  border: 1px solid #ddd;
  border-radius: 20px;
  padding: 16px;
  font-size: 16px;
  resize: none;
  min-height: 56px;
  max-height: 200px;
  outline: none;
  transition: border-color 0.3s;
  overflow-y: auto;
  background-color: white;
  color: #333;
  scrollbar-width: none;
  -ms-overflow-style: none;

  &::-webkit-scrollbar {
    display: none;
  }

  &:focus {
    border-color: #007bff;
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.send-button {
  margin-left: 12px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 20px;
  padding: 0 24px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
  height: 56px;
  align-self: center;
  flex-shrink: 0;

  &:hover:not(:disabled) {
    background-color: #0069d9;
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.typing-indicator {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 17px;
  margin-left: 2px;
}

.typing-indicator .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: currentColor;
  display: inline-block;
  margin: 0 2px;
  opacity: 0.6;
  animation: bounce 1.4s infinite ease-in-out;

  &:nth-child(1) {
    animation-delay: -0.32s;
  }

  &:nth-child(2) {
    animation-delay: -0.16s;
  }
}

@keyframes bounce {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.6;
  }
  30% {
    transform: translateY(-5px);
    opacity: 1;
  }
}

/* Markdown内容样式 - 修复深度选择器警告 */
:deep(.message-content) {
  p {
    margin: 0.5em 0;
  }

  h1, h2, h3, h4, h5, h6 {
    margin-top: 0.8em;
    margin-bottom: 0.5em;
    font-weight: bold;
  }

  code {
    font-family: Consolas, Monaco, monospace;
    background-color: rgba(0, 0, 0, 0.06);
    padding: 0.1em 0.3em;
    border-radius: 3px;
  }

  pre {
    font-family: Consolas, Monaco, monospace;
    background-color: #f8f8f8;
    padding: 12px;
    border-radius: 4px;
    overflow-x: auto;
    margin: 10px 0;

    code {
      background-color: transparent;
      padding: 0;
    }
  }

  ul, ol {
    padding-left: 1.8em;
    margin: 0.5em 0;

    li {
      margin: 0.3em 0;
    }
  }

  blockquote {
    border-left: 3px solid #ddd;
    padding-left: 1em;
    margin: 0.5em 0;
    color: #555;
  }

  table {
    border-collapse: collapse;
    width: 100%;
    margin: 10px 0;

    th, td {
      border: 1px solid #ddd;
      padding: 8px;
      text-align: left;
    }

    th {
      background-color: #f5f5f5;
      font-weight: bold;
    }
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .message {
    max-width: 95%;
  }

  .message-content {
    font-size: 15px;
  }

  .chat-input-container {
    padding: 12px;
  }

  .input-box {
    padding: 12px;
  }

  .send-button {
    padding: 0 18px;
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .avatar {
    width: 32px;
    height: 32px;
  }

  .ai-avatar::after {
    font-size: 16px;
  }

  .message-bubble {
    padding: 10px;
  }

  .message-content {
    font-size: 14px;
  }

  .typing-indicator .dot {
    width: 5px;
    height: 5px;
  }

  .input-box {
    min-height: 48px;
    padding: 12px;
  }

  .send-button {
    height: 48px;
  }
}

/* 连续消息气泡样式 */
.ai-message + .ai-message {
  margin-top: 4px;

  .avatar {
    visibility: hidden;
  }

  .message-bubble {
    border-top-left-radius: 10px;
  }
}
</style>
