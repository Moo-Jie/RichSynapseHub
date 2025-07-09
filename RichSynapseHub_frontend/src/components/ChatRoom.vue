<template>
  <div class="chat-container">
    <!-- 聊天记录区域 -->
    <div class="chat-messages" ref="messagesContainer">
      <div v-for="(msg, index) in messages" :key="index" class="message-wrapper">
        <!-- AI消息 -->
        <div v-if="!msg.isUser"
             class="message ai-message"
             :class="[msg.type]">
          <div class="avatar ai-avatar">
            <!-- AI头像占位符 -->
          </div>
          <div class="message-bubble">
            <div class="message-content">
              <!-- 使用v-html渲染Markdown解析后的内容 -->
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
  gfm: true,           // 支持GitHub风格的Markdown
  breaks: true,        // 支持回车换行
  headerIds: false     // 不生成标题ID
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

// 统一处理Markdown解析（用户和AI消息）
const getParsedContent = (msg) => {
  // 版本标识解决缓存问题
  const cacheKey = `${msg.content}_${msg.time}`;

  if (contentCache.has(cacheKey)) {
    return contentCache.get(cacheKey);
  }

  // 使用marked解析Markdown
  const rawHTML = marked.parse(msg.content);

  // 使用DOMPurify消毒防止XSS攻击
  const cleanHTML = DOMPurify.sanitize(rawHTML);

  // 缓存结果
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
// 在消息更新时强制刷新
watch(() => props.messages, () => {
  contentCache.clear();
}, {deep: true});
watch(() => props.messages.length, scrollToBottom)
watch(() => props.messages.map(m => m.content).join(''), scrollToBottom)

//  输入框高度
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

<style scoped lang="scss">:deep(.message-content pre) {
  background-color: #f8f8f8;
  padding: 12px;
  border-radius: 4px;
  overflow-x: auto;
  margin: 10px 0;
  font-family: 'Consolas', monospace;
}

:deep(.message-content code) {
  background-color: #f8f8f8;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: 'Consolas', monospace;
}

:deep(.message-content blockquote) {
  border-left: 3px solid #ddd;
  padding-left: 12px;
  margin: 8px 0;
  color: #666;
}

:deep(.message-content table) {
  border-collapse: collapse;
  width: 100%;
  margin: 10px 0;
}

:deep(.message-content pre) {
  background-color: #f8f8f8;
  padding: 12px;
  border-radius: 4px;
  overflow-x: auto;
  margin: 10px 0;
  font-family: 'Consolas', monospace;
}

:deep(.message-content) {
  th, td {
    border: 1px solid #ddd;
    padding: 8px;
    text-align: left;
  }
}

::v-deep .message-content code {
  background-color: #f8f8f8;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: 'Consolas', monospace;
}

::v-deep .message-content blockquote {
  border-left: 3px solid #ddd;
  padding-left: 12px;
  margin: 8px 0;
  color: #666;
}

:deep(.message-content) {
  th {
    background-color: #f5f5f5;
    font-weight: bold;
  }
}

:deep(.message-content p) {
  margin: 0.5em 0;
}

:deep(.message-content h1),
:deep(.message-content h2),
:deep(.message-content h3),
:deep(.message-content h4),
:deep(.message-content h5),
:deep(.message-content h6) {
  margin-top: 0.8em;
  margin-bottom: 0.5em;
  font-weight: bold;
}

:deep(.message-content code) {
  font-family: Consolas, Monaco, monospace;
  background-color: rgba(0, 0, 0, 0.06);
  padding: 0.1em 0.3em;
  border-radius: 3px;
}

:deep(.message-content pre) {
  font-family: Consolas, Monaco, monospace;
  background-color: rgba(0, 0, 0, 0.05);
  padding: 0.8em 1em;
  border-radius: 4px;
  overflow-x: auto;
  margin: 0.5em 0;
}

:deep(.message-content ul),
:deep(.message-content ol) {
  padding-left: 1.8em;
  margin: 0.5em 0;
}

:deep(.message-content li) {
  margin: 0.3em 0;
}

:deep(.message-content blockquote) {
  border-left: 3px solid #ddd;
  padding-left: 1em;
  margin: 0.5em 0;
  color: #555;
}

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
  margin-left: auto; /* 用户消息靠右 */
  flex-direction: row; /* 正常顺序，先气泡后头像 */
}

.ai-message {
  margin-right: auto; /* AI消息靠左 */
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
  margin-left: 8px; /* 用户头像在右侧，左边距 */
}

/* 修改AI头像样式 - 添加渐变背景和"AI"文字 */
:deep(.ai-avatar) {
  background: linear-gradient(135deg, #4361ee, #c8e5ff); /* 使用与标题相同的渐变色 */
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;

  &::after {
    content: 'AI'; /* 显示"AI"文字 */
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    color: white;
    font-size: 18px; /* 适当 字体大小 */
    font-weight: bold;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2); /* 添加文字阴影增强可读性 */
  }
}

.ai-avatar {
  margin-right: 8px; /* AI头像在左侧，右边距 */
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
  min-width: 100px; /* 最小宽度 */
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
  min-height: 56px; /* 提高最小高度 */
  max-height: 200px; /* 增加最大高度 */
  outline: none;
  transition: border-color 0.3s;
  overflow-y: auto;
  background-color: white; /* 白底 */
  color: #333; /* 黑字 */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE & Edge */
}

/* 隐藏Webkit浏览器的滚动条 */
.input-box::-webkit-scrollbar {
  display: none;
}

.input-box:focus {
  border-color: #007bff;
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
}

.send-button:hover:not(:disabled) {
  background-color: #0069d9;
}

/* 新增的灵动三点式输入指示器 */
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
}

.typing-indicator .dot:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-indicator .dot:nth-child(2) {
  animation-delay: -0.16s;
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

.input-box:disabled, .send-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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

  :deep(.ai-avatar) {
    width: 36px;
    height: 36px;

    &::after {
      font-size: 16px; /* 移动端稍小字体 */
    }
  }
}

/* 连续消息气泡样式 */
.ai-message + .ai-message {
  margin-top: 4px;
}

.ai-message + .ai-message .avatar {
  visibility: hidden;
}

.ai-message + .ai-message .message-bubble {
  border-top-left-radius: 10px;
}

/* Markdown内容样式 */
.message-content >>> p {
  margin: 0.5em 0;
}

.message-content >>> h1,
.message-content >>> h2,
.message-content >>> h3,
.message-content >>> h4,
.message-content >>> h5,
.message-content >>> h6 {
  margin-top: 0.8em;
  margin-bottom: 0.5em;
  font-weight: bold;
}

.message-content >>> code {
  font-family: Consolas, Monaco, monospace;
  background-color: rgba(0, 0, 0, 0.06);
  padding: 0.1em 0.3em;
  border-radius: 3px;
}

.message-content >>> pre {
  font-family: Consolas, Monaco, monospace;
  background-color: rgba(0, 0, 0, 0.05);
  padding: 0.8em 1em;
  border-radius: 4px;
  overflow-x: auto;
  margin: 0.5em 0;
}

.message-content >>> ul,
.message-content >>> ol {
  padding-left: 1.8em;
  margin: 0.5em 0;
}

.message-content >>> li {
  margin: 0.3em 0;
}

.message-content >>> blockquote {
  border-left: 3px solid #ddd;
  padding-left: 1em;
  margin: 0.5em 0;
  color: #555;
}
</style>
