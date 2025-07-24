<template>
  <div class="interView-master-container" :class="{ 'page-loaded': !isLoading }">
    <div class="decorative-elements">
      <div v-for="i in 12" :key="i" class="floating-shape" :class="`shape-${i}`"></div>
    </div>
    <div class="content-wrapper">
      <div class="decorative-elements">
        <div v-for="i in 8" :key="i" class="floating-shape" :class="`shape-${i}`"></div>
      </div>
      <div class="header">
        <div class="light-beam"></div>
      </div>
      <div class="chat-area">
        <br>
        <h1 class="glitch-title">
            <span
                v-for="(text, idx) in titleWords"
                :key="idx"
                :style="{ animationDelay: `${0.15 * (idx + 1)}s` }"
            >{{ text }}</span>
        </h1>
        <div class="header">
          <button class="app-button back-button" @click.stop="goBack">
            <span>返回主页</span>
            <span class="btn-icon">←</span>
          </button>
        </div>
        <ChatRoom
            :messages="messages"
            :connection-status="connectionStatus"
            :parse-markdown="parseMarkdown"
            @send-message="sendMessage"
        />
      </div>
    </div>
    <AppFooter/>
  </div>
</template>

<script setup>
import {onBeforeUnmount, onMounted, ref} from 'vue'
import {useHead} from '@vueuse/head'
import {useRouter} from 'vue-router'
import ChatRoom from '../components/ChatRoom.vue'
import AppFooter from './AppFooter.vue'
import {chatWithManus} from '../api'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'

const router = useRouter()
const isLoading = ref(true)
onMounted(() => {
  setTimeout(() => {
    isLoading.value = false
  }, 150)
})
// 返回主页
const goBack = (event) => {
  event.stopPropagation()
  router.push('/')
}

// 创建Markdown解析器实例
const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight: (str, lang) => {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return `<pre class="hljs"><code>${hljs.highlight(str, {language: lang}).value}</code></pre>`
      } catch (__) {
      }
    }
    return `<pre class="hljs"><code>${md.utils.escapeHtml(str)}</code></pre>`
  }
})

// Markdown解析函数
const parseMarkdown = (content) => {
  return md.render(content)
}

// 设置页面标题和元数据
useHead({
  title: '自主规划智能体 - RichSynapseHub',
  meta: [
    {
      name: 'description',
      content: '自主规划智能体是 RichSynapseHub 的高级AI规划系统，能够自主规划任务路径，解决复杂问题'
    },
    {
      name: 'keywords',
      content: '自主规划智能体,AI规划系统,任务规划,问题解决,AI智能体,RichSynapseHub'
    }
  ]
})

const messages = ref([])
const connectionStatus = ref('disconnected')
const titleWords = ref(["Rich", 'Synapse', 'Hub -', '自主', '规划', '智能体']);

let eventSource = null

// 添加消息到列表
const addMessage = (content, isUser) => {
  messages.value.push({
    content,
    isUser,
    time: new Date().getTime()
  })
}

// 发送消息
const sendMessage = (message) => {
  addMessage(message, true)

  // 连接SSE
  if (eventSource) {
    eventSource.close()
  }

  // 设置连接状态
  connectionStatus.value = 'connecting'

  // 消息缓冲和分割逻辑
  let messageBuffer = []
  let lastBubbleTime = Date.now()
  let isFirstResponse = true

  const chineseEndPunctuation = ['。', '！', '？', '…']
  const minBubbleInterval = 800

  // 创建消息气泡的函数
  const createBubble = (content) => {
    if (!content.trim()) return

    const now = Date.now()
    const timeSinceLastBubble = now - lastBubbleTime

    if (isFirstResponse) {
      addMessage(content, false)
      isFirstResponse = false
    } else if (timeSinceLastBubble < minBubbleInterval) {
      setTimeout(() => {
        addMessage(content, false)
      }, minBubbleInterval - timeSinceLastBubble)
    } else {
      addMessage(content, false)
    }

    lastBubbleTime = Date.now()
    messageBuffer = []
  }

  eventSource = chatWithManus(message)

  // 监听SSE消息
  eventSource.onmessage = (event) => {
    const data = event.data

    if (data && data !== '[DONE]') {
      messageBuffer.push(data)

      // 检查是否应该创建新气泡
      const combinedText = messageBuffer.join('')
      const lastChar = data.charAt(data.length - 1)
      const hasCompleteSentence = chineseEndPunctuation.includes(lastChar) || data.includes('\n\n')
      const isLongEnough = combinedText.length > 40

      if (hasCompleteSentence || isLongEnough) {
        createBubble(combinedText)
      }
    }

    if (data === '[DONE]') {
      // 如果还有未显示的内容，创建最后一个气泡
      if (messageBuffer.length > 0) {
        const remainingContent = messageBuffer.join('')
        createBubble(remainingContent)
      }

      // 完成后关闭连接
      connectionStatus.value = 'disconnected'
      eventSource.close()
    }
  }

  // 监听SSE错误
  eventSource.onerror = (error) => {
    // console.error('SSE Error:', error)
    connectionStatus.value = 'error'
    eventSource.close()

    // 如果出错时有未显示的内容，也创建气泡
    if (messageBuffer.length > 0) {
      const remainingContent = messageBuffer.join('')
      createBubble(remainingContent)
    }
  }
}

// 页面加载时添加欢迎消息
onMounted(() => {
  addMessage('你好，我是自主规划智能体。我能通过分步规划解决复杂问题，请问有什么可以帮您的？', false)
})

// 组件销毁前关闭SSE连接
onBeforeUnmount(() => {
  if (eventSource) {
    eventSource.close()
  }
})
</script>

<style scoped lang="scss">



.glitch-title {
  font-family: 'Short Stack', 'Comic Neue', cursive;
  font-size: clamp(2.0rem, 3.5vw, 3.2rem);
  font-weight: 800;
  color: rgba(255, 255, 255, 0.9);
  background: linear-gradient(135deg, #4361ee, #ffffff);
  -webkit-background-clip: text;
  background-clip: text;
  letter-spacing: -0.03em;
  position: relative;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: transform 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
  -webkit-text-stroke: 0.5px #4361ee;
}

.interView-master-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: radial-gradient(circle at top left, rgba(248, 249, 250, 0.8), rgba(233, 236, 239, 0.9));
  position: relative;
  padding-bottom: 200px;
  overflow: hidden;
  z-index: 0;
  font-family: 'Segoe UI', /* Windows优先 */
  -apple-system, /* macOS/iOS系统字体 */
  'SF Pro Text', /* macOS备选 */
  'Tahoma', /* Windows备用 */
  'Geneva',
  'Verdana',
  'Noto Sans', /* 跨平台开源字体 */
  sans-serif; /* 最终回退 */
}

.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.9);
  z-index: 9999;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: opacity 0.6s ease;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #4361ee;
  border-radius: 50%;
  border-top-color: transparent;
  animation: spin 1s linear infinite;
}

.interView-master-container {
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.8s cubic-bezier(0.23, 1, 0.32, 1);
}

.interView-master-container.page-loaded {
  opacity: 1;
  transform: translateY(0);
}

.header {
  text-align: center;
  margin-bottom: 2rem;
  padding-top: 3rem;
  position: relative;
}

.light-beam {
  position: absolute;
  top: -50%;
  left: 50%;
  transform: translateX(-50%);
  height: 200%;
  width: 120%;
  background: radial-gradient(ellipse at center, rgb(255, 255, 255), transparent 80%);
  z-index: -1;
  animation: beamPulse 8s infinite alternate ease-in-out;
}

.glitch-wrapper {
  position: relative;
  display: inline-block;
  perspective: 800px;
}

.glitch-title span {
  position: relative;
  display: inline-block;
  animation: floatText 4s ease-in-out infinite;
  backface-visibility: hidden;
  transform: translateZ(0);
}

.glitch-title:hover {
  transform: scale(1.03) rotateX(5deg) rotateY(5deg);
}

.content-wrapper {
  display: flex;
  flex-direction: column;
  flex: 1;
  padding: 0 1.5rem;
  position: relative;
  z-index: 2;
}

.chat-area {
  flex: 1;
  padding: 1.5rem;
  overflow: hidden;
  position: relative;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 24px;
  box-shadow: 0 8px 32px rgba(31, 38, 135, 0.15),
  0 0 0 1px rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(6px);
  min-height: calc(100vh - 56px - 180px);
  margin-bottom: 300px;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    right: -50%;
    bottom: -50%;
    background: linear-gradient(
            45deg,
            rgba(255, 230, 240, 0.6) 0%,
            rgba(220, 240, 255, 0.6) 50%,
            rgba(255, 240, 220, 0.6) 100%
    );
    animation: gradientFlow 12s ease infinite;
    z-index: -1;
    transform: rotate(15deg);
  }
}

@keyframes gradientFlow {
  0% {
    transform: rotate(15deg) translate(-10%, -10%);
  }
  50% {
    transform: rotate(15deg) translate(10%, 10%);
  }
  100% {
    transform: rotate(15deg) translate(-10%, -10%);
  }
}

.footer-container {
  margin-top: auto;
  position: relative;
  z-index: 2;
}

/* 高级头像样式 */
:deep(.message-avatar) {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
  transition: all 0.3s cubic-bezier(0.2, 0.8, 0.4, 1.3);
  position: relative;
  overflow: hidden;
}

:deep(.ai-avatar-fallback) {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
  color: white;

  &.interView {
    background: #bddeff;
  }

  &:not(.interView) {
    background: #4361ee;
  }
}

:deep(.user-avatar) {
  background: #4361ee;

  &::after {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    color: white;
    font-size: 24px;
    font-weight: bold;
  }
}

/* 头像优化 */
:deep(.ai-avatar) {
  background: #ffffff url('../assets/LOGO.png') no-repeat center center;
  background-size: 100%;

  &::after {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    color: white;
    font-size: 24px;
    font-weight: bold;
  }
}

:deep(.message-avatar:hover) {
  transform: scale(1.1);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

/* 输入框美化 */
:deep(.message-input) {
  background-color: white; /* 白底 */
  color: black; /* 黑字 */
  border: 2px solid #e0e0e0;
  border-radius: 18px;
  padding: 16px 20px; /* 增加高度 */
  min-height: 60px; /* 设置最小高度 */
  font-size: 1.15rem;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

:deep(.message-input:focus) {
  border-color: #4361ee;
  box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.15);
  outline: none;
}

:deep(.send-button) {
  background: linear-gradient(135deg, #4361ee, #ffffff);
  color: white;
  border: none;
  border-radius: 18px;
  padding: 12px 24px;
  font-size: 1.1rem;
  font-weight: 300;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.2, 0.8, 0.4, 1.3);
  box-shadow: 0 4px 12px rgba(67, 97, 238, 0.2);
}

:deep(.send-button:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(67, 97, 238, 0.3);
}

:deep(.send-button:active) {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(67, 97, 238, 0.2);
}

/* 消息动画效果 */
:deep(.message-container) {
  transform-origin: bottom;
}

@keyframes messageAppear {
  0% {
    opacity: 0;
    transform: translateY(20px) scale(0.95);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

:deep(.message-content h1) {
  font-size: 1.9rem;
  margin: 1.5rem 0 1rem;
  border-bottom: 2px solid #4361ee;
  padding-bottom: 0.5rem;
}

:deep(.message-content h2) {
  font-size: 1.7rem;
  margin: 1.3rem 0 0.8rem;
}

:deep(.message-content h3) {
  font-size: 1.5rem;
  margin: 1.1rem 0 0.7rem;
}

:deep(.message-content p) {
  margin-bottom: 1.2rem;
}

:deep(.message-content ul),
:deep(.message-content ol) {
  padding-left: 1.8rem;
  margin-bottom: 1.2rem;
}

:deep(.message-content li) {
  margin-bottom: 0.6rem;
}

:deep(.message-content code) {
  background-color: rgba(67, 97, 238, 0.1);
  padding: 0.2rem 0.4rem;
  border-radius: 4px;
  font-family: 'Segoe UI', /* Windows优先 */
  -apple-system, /* macOS/iOS系统字体 */
  'SF Pro Text', /* macOS备选 */
  'Tahoma', /* Windows备用 */
  'Geneva',
  'Verdana',
  'Noto Sans', /* 跨平台开源字体 */
  sans-serif; /* 最终回退 */
  font-size: 1.05rem;
}

:deep(.message-content pre) {
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 1rem;
  overflow-x: auto;
  margin: 1.5rem 0;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  border-left: 4px solid #4361ee;
}

:deep(.message-content blockquote) {
  border-left: 4px solid #4361ee;
  margin: 1.5rem 0;
  color: #555;
  background-color: rgba(67, 97, 238, 0.05);
  border-radius: 0 8px 8px 0;
  padding: 0.8rem 1.2rem;
}

:deep(.message-content table) {
  width: 100%;
  border-collapse: collapse;
  margin: 1.5rem 0;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
}

:deep(.message-content th),
:deep(.message-content td) {
  border: 1px solid #dee2e6;
  padding: 0.75rem;
  text-align: left;
}

:deep(.message-content th) {
  background-color: rgba(67, 97, 238, 0.1);
  font-weight: 600;
}

:deep(.message-content tr:nth-child(even)) {
  background-color: rgba(67, 97, 238, 0.05);
}

/* 在移动端  */
@media (max-width: 480px) {
  :deep(.message-input) {
    padding: 14px 16px;
    min-height: 50px; /* 移动端最小高度 */
    font-size: 1rem;
    color: black; /* 确保移动端黑字 */
  }

  .chat-area {
    padding: 1rem;
    min-height: calc(100vh - 42px - 150px);
    margin-bottom: 300px;
  }

  .footer-container {
    padding-bottom: 1.5rem;
  }

  :deep(.message-content) {
    font-size: 1.05rem;
  }

  :deep(.message-avatar) {
    width: 36px;
    height: 36px;
    font-size: 20px;
  }

  :deep(.message-input) {
    padding: 12px 16px;
    font-size: 1rem;
  }

  :deep(.send-button) {
    padding: 10px 18px;
    font-size: 1rem;
  }
}

/* Decorative elements */
.decorative-elements {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  pointer-events: none;
  z-index: -1;
  overflow: hidden;
}

.floating-shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(30px);
  opacity: 0.15;
  animation: float 15s infinite alternate;
}

.shape-1 {
  width: 300px;
  height: 300px;
  background: linear-gradient(45deg, #ffffff, #c8e5ff);
  top: 15%;
  right: -50px;
  animation: float 20s infinite alternate cubic-bezier(0.6, 0, 0.4, 1);
  @extend .shape-1;
}

.shape-2 {
  width: 220px;
  height: 220px;
  background: linear-gradient(45deg, #4361ee, #4cc9f0);
  bottom: 15%;
  left: -40px;
  animation: float 25s infinite alternate-reverse cubic-bezier(0.6, 0, 0.4, 1);
  @extend .shape-2;
}

.shape-3 {
  width: 180px;
  height: 180px;
  background: linear-gradient(45deg, #8188f8, #ffffff);
  top: 35%;
  left: 25%;
  animation: float 18s infinite cubic-bezier(0.6, 0, 0.4, 1);
}

.shape-4 {
  width: 150px;
  height: 150px;
  background: linear-gradient(45deg, #4cc9f0, #4361ee);
  bottom: 25%;
  right: 20%;
  animation: float 22s infinite alternate cubic-bezier(0.6, 0, 0.4, 1);
}

.shape-5 {
  width: 100px;
  height: 200px;
  background: linear-gradient(45deg, #ffffff, #4cc9f0);
  top: 55%;
  right: 30%;
  animation: float 30s infinite alternate-reverse cubic-bezier(0.6, 0, 0.4, 1);
}

.shape-6 {
  width: 250px;
  height: 120px;
  background: linear-gradient(45deg, #e9ccff, #4cc9f0);
  top: 20%;
  left: 20%;
  animation: float 25s infinite cubic-bezier(0.6, 0, 0.4, 1);
}

.shape-7 {
  width: 180px;
  height: 180px;
  background: linear-gradient(45deg, #4361ee, #ffffff);
  bottom: 35%;
  left: 60%;
  animation: float 17s infinite alternate-reverse cubic-bezier(0.6, 0, 0.4, 1);
}

.shape-8 {
  width: 80px;
  height: 80px;
  background: linear-gradient(45deg, #4cc9f0, #ffffff);
  top: 65%;
  right: 40%;
  animation: float 28s infinite cubic-bezier(0.6, 0, 0.4, 1);
}

.shape-9 {
  width: 120px;
  height: 120px;
  background: linear-gradient(45deg, #ff6b6b, #ff8787);
  top: 40%;
  right: 15%;
  animation-delay: 3s;
}

.shape-10 {
  width: 180px;
  height: 180px;
  background: linear-gradient(45deg, #69db7c, #b2f2bb);
  bottom: 30%;
  left: 10%;
}

.shape-11 {
  width: 220px;
  height: 220px;
  background: linear-gradient(45deg, #748ffc, #bac8ff);
  top: 20%;
  left: 30%;
  animation-duration: 18s;
}

.shape-12 {
  width: 150px;
  height: 150px;
  background: linear-gradient(45deg, #ff922b, #ffc078);
  bottom: 25%;
  right: 30%;
}

/* Animations */
@keyframes float {
  0% {
    transform: translate(0, 0) rotate(0deg) scale(1);
  }
  20% {
    transform: translate(60px, -80px) rotate(5deg) scale(1.05);
  }
  40% {
    transform: translate(-40px, 60px) rotate(-3deg) scale(0.95);
  }
  60% {
    transform: translate(80px, 20px) rotate(8deg) scale(1.1);
  }
  80% {
    transform: translate(-20px, -60px) rotate(-5deg) scale(0.98);
  }
  100% {
    transform: translate(0, 0) rotate(0deg) scale(1);
  }
}

@keyframes beamPulse {
  0% {
    opacity: 0.6;
    transform: translateX(-50%) scale(1);
  }
  50% {
    opacity: 0.3;
    transform: translateX(-50%) scale(1.1);
  }
  100% {
    opacity: 0.6;
    transform: translateX(-50%) scale(1);
  }
}

@keyframes floatText {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
    text-shadow: 0 0 0 rgba(114, 9, 183, 0);
  }
  50% {
    transform: translateY(-12px) rotate(2deg);
    text-shadow: 0 8px 15px rgb(255, 255, 255);
  }
}

@media (max-width: 480px) {
  .header {
    padding: 1.5rem 0.5rem 0.5rem;
  }

  .glitch-title {
    font-family: 'Comic Neue', cursive;
    font-size: clamp(2rem, 8vw, 3rem);
  }

  .chat-area {
    padding: 0.8rem;
    min-height: calc(100vh - 42px - 150px);
    margin-bottom: 0.8rem;
  }

  .decorative-elements {
    display: none;
  }
}

/* 输入指示器样式 */
:deep(.typing-indicator) {
  display: inline-flex;
  align-items: center;
  height: 20px;
  padding-left: 8px;

  span {
    width: 8px;
    height: 8px;
    margin: 0 2px;
    background: #4361ee;
    border-radius: 50%;
    animation: scaleUpDown 1.2s infinite ease-in-out;

    &:nth-child(1) {
      animation-delay: 0s
    }

    &:nth-child(2) {
      animation-delay: 0.2s
    }

    &:nth-child(3) {
      animation-delay: 0.4s
    }
  }
}

@keyframes scaleUpDown {
  0%, 80%, 100% {
    transform: scale(0.4);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.back-button {
  position: absolute;
  top: 1.5rem;
  right: 1.5rem;
  background: linear-gradient(135deg, #8188f8, #59a7ff) !important;
  padding: 0.8rem 1.5rem !important;
  z-index: 1000;
  transition: all 0.3s cubic-bezier(0.2, 0.8, 0.4, 1.3);

  &:hover {
    transform: translateY(-2px) scale(1.05);
    box-shadow: 0 6px 20px rgb(200, 229, 255);
  }

  .btn-icon {
    margin-left: 0.5rem;
    transition: transform 0.3s ease;
  }
}

@media (max-width: 480px) {
  .back-button {
    top: 1rem;
    right: 1rem;
    padding: 0.6rem 1.2rem !important;
    font-size: 0.9rem;
  }

  :deep(.typing-indicator) {
    span {
      width: 6px;
      height: 6px;
    }
  }
}
</style>