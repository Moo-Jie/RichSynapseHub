<template>
  <div class="home-container" :class="{ 'page-loaded': !isLoading }">
    <div class="decorative-elements">
      <div v-for="i in 8" :key="i" class="floating-shape" :class="`shape-${i}`"></div>
    </div>

    <div class="header">
      <nav class="user-nav">
        <template v-if="userStore.userInfo">
          <el-dropdown>
            <div class="user-profile">
              <img :src="userStore.userInfo.avatar" class="user-avatar" alt="avatar"/>
              <span class="username">{{ truncateUsername(userStore.userInfo.username) }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="userStore.logout()">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <router-link to="/login" class="nav-link">登录</router-link>
          <span class="nav-divider">|</span>
          <router-link to="/register" class="nav-link">注册</router-link>
        </template>
      </nav>
      <div class="light-beam"></div>
      <div class="glitch-wrapper">
        <h1 class="glitch-title">
          <img src="/src/assets/LOGO.png" alt="LOGO" class="logo">
        </h1>
        <h1 class="glitch-title">
          <span
              v-for="(text, idx) in titleWords"
              :key="idx"
              :style="{ animationDelay: `${0.15 * (idx + 1)}s` }">{{ text }}
          </span>
        </h1>
      </div>
      <p class="subtitle">{{ subtitle }}</p>
    </div>

    <div class="apps-container">
      <app-card
          v-for="app in apps"
          :key="app.id"
          :icon="app.icon"
          :title="app.title"
          :description="app.description"
          @click="navigateTo(app.path)"
      />
    </div>

    <app-footer/>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useHead } from '@vueuse/head';
import AppCard from './AppCard.vue';
import AppFooter from './AppFooter.vue';
import { useUserStore } from '../stores/user';

const userStore = useUserStore();
const router = useRouter();
onMounted(() => {
  setTimeout(() => {
    isLoading.value = false;
  }, 150);
});
// 用户名截断函数（优化版）
const truncateUsername = (name) => {
  const maxLen = window.innerWidth <= 480 ? 8 : 12;
  return name.length > maxLen ? `${name.substring(0, maxLen)}..` : name;
};
const titleWords = ref(['Rich', 'Synapse', 'Hub']);
const subtitle = ref('一个实用的 AI 智能体应用集');
const isLoading = ref(true);
const apps = ref([
  {
    id: 1,
    icon: "⬜",
    title: 'AI 面试专家',
    description: '智能面试顾问，帮你解答面试烦恼',
    path: '/interView-master',
    theme: 'linear-gradient(135deg, #597ef7, #85a5ff)',
    btnText: '开始面试'
  },
  {
    id: 2,
    icon: "⬜",
    title: 'AI 自主规划智能体',
    description: '全能型AI助手，解决各类专业问题',
    path: '/super-agent',
    theme: 'linear-gradient(135deg, #722ed1, #9254de)',
    btnText: '立即体验'
  }
]);
const navigateTo = (path) => {
  router.push(path);
}

useHead({
  title: 'RichSynapseHub - 首页',
  meta: [
    {name: 'description', content: '提供AI面试专家和 AI 自主规划智能体服务，满足您的各类AI对话需求'},
    {name: 'keywords', content: 'AI智能体,AI应用,AI面试专家,AI助手,智能对话'}
  ]
});
</script>

<style scoped>
/* 导航栏 */
.user-nav {
  position: absolute;
  top: 20px;
  right: 30px;
  display: flex;
  gap: 15px;
  align-items: center;
  z-index: 100;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.user-profile:hover {
  transform: translateY(-2px);
}

.user-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #4361ee;
  box-shadow: 0 2px 8px rgba(67, 97, 238, 0.25);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.user-avatar:hover {
  transform: scale(1.1);
  border-color: #ffffff;
  box-shadow: 0 4px 12px rgb(255, 255, 255);
}

.username {
  color: #4361ee;
  font-weight: 500;
  font-size: 1.1rem;
  letter-spacing: 0.25px;
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  text-shadow: 0 1px 2px rgba(67, 97, 238, 0.15);
}

.user-profile:hover .username {
  color: #001a80;
  text-shadow: 0 2px 6px rgba(114, 9, 183, 0.25);
}

/* 移动端适配 */
@media (max-width: 480px) {
  .user-avatar {
    width: 32px;
    height: 32px;
    border-width: 1px;
  }

  .username {
    font-size: 0.9rem;
    max-width: 90px;
    font-weight: 600;
  }

  .user-nav {
    right: 15px;
    gap: 10px;
  }
}

.nav-link {
  color: #4361ee;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s ease;
  padding: 8px 16px;
  border-radius: 8px;
}

.nav-link:hover {
  background: rgba(67, 97, 238, 0.1);
  transform: translateY(-2px);
}

.nav-divider {
  color: #adb5bd;
}

/* 以下样式保持不变 */
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

.home-container {
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.8s cubic-bezier(0.23, 1, 0.32, 1);
}

.home-container.page-loaded {
  opacity: 1;
  transform: translateY(0);
}

.home-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem 2rem 400px;
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  background: radial-gradient(circle at top left, rgba(248, 249, 250, 0.8), rgba(233, 236, 239, 0.9));
  z-index: 0;
}

.apps-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 2.5rem;
  margin-bottom: 12rem;
  perspective: 1200px;
}

.header {
  text-align: center;
  margin-bottom: 4rem;
  padding-top: 3rem;
  position: relative;
}

.app-card {
  position: relative;
  opacity: 0;
  transform: translateY(30px) rotateX(-15deg);
  animation: cardEnter 0.6s cubic-bezier(0.23, 1, 0.32, 1) forwards;
  transition: all 0.5s cubic-bezier(0.19, 1.04, 0.58, 1.15);
  will-change: transform;
}

.light-beam {
  position: absolute;
  top: -50%;
  left: 50%;
  transform: translateX(-50%);
  height: 200%;
  width: 120%;
  background: radial-gradient(ellipse at center, rgba(67, 97, 238, 0.15), transparent 80%);
  z-index: -1;
  animation: beamPulse 8s infinite alternate ease-in-out;
}

.glitch-wrapper {
  position: relative;
  display: inline-block;
  margin-bottom: 2rem;
  perspective: 800px;
}

.glitch-title {
  font-family: 'Poppins', sans-serif;
  font-size: clamp(2.8rem, 5.5vw, 4.2rem);
  font-weight: 800;
  color: rgba(255, 255, 255, 0.9);
  background: linear-gradient(135deg, #c8e5ff, #ffffff);
  -webkit-background-clip: text;
  background-clip: text;
  letter-spacing: -0.03em;
  position: relative;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: transform 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
  -webkit-text-stroke: 0.5px #4361ee;
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

.subtitle {
  font-size: clamp(1.1rem, 3.2vw, 1.35rem);
  color: #495057;
  margin: 1.5rem auto 0;
  max-width: 600px;
  background: linear-gradient(to right, transparent, rgba(67, 97, 238, 0.15), transparent);
  padding: 0.8rem 0;
  position: relative;
  font-weight: 300;
  backdrop-filter: blur(4px);
  border-radius: 100px;
}

.subtitle::before,
.subtitle::after {
  content: '✦';
  position: absolute;
  color: rgb(255, 255, 255);
  font-size: 1.2em;
  animation: spin 8s linear infinite;
  filter: drop-shadow(0 0 3px rgba(67, 97, 238, 0.3));
}

.subtitle::before {
  left: 0;
  transform: translateX(-200%);
}

.subtitle::after {
  right: 0;
  transform: translateX(200%);
}

.apps-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 2.5rem;
  margin-bottom: 4rem;
  perspective: 1200px;
}

@keyframes borderFlow {
  to {
    background-position: 200% 0;
  }
}

.app-card::before {
  content: '';
  position: absolute;
  inset: -6px;
  background: linear-gradient(45deg,
  rgba(67, 97, 238, 0.8),
  rgb(255, 255, 255),
  rgb(255, 255, 255));
  background-size: 200% auto;
  z-index: -1;
  border-radius: var(--border-radius);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.logo {
  width: 75px;
  height: auto;
  margin-right: 1.5rem;
  vertical-align: middle;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
  transition: all 0.3s cubic-bezier(0.2, 0.8, 0.4, 1.3);
  position: absolute;
  left: -80px;
  top: 50%;
  animation: floatText 4s ease-in-out infinite;
  backface-visibility: hidden;
  transform: translateZ(0);
  background: linear-gradient(135deg, #4361ee, #ffffff);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-stroke: 0.5px #4361ee;
}

.logo:hover {
  transform: translateY(-50%) scale(1.1) rotate(5deg);
  filter: drop-shadow(0 4px 8px rgb(255, 255, 255));
}

.app-card:hover {
  box-shadow: 0 0 0 3px rgba(67, 97, 238, 0.3),
  0 0 48px rgba(67, 97, 238, 0.3),
  0 16px 32px rgba(0, 0, 0, 0.2);
  transform: translateY(-8px) scale(1.03) rotateZ(0.5deg);
}

.app-card:hover::before {
  opacity: 1;
  animation: borderFlow 3s linear infinite;
  filter: hue-rotate(5deg);
}

@keyframes cardEnter {
  to {
    opacity: 1;
    transform: translateY(0) rotateX(0);
  }
}

.app-card:nth-child(1) {
  animation-delay: 0.3s;
}

.app-card:nth-child(2) {
  animation-delay: 0.5s;
}

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
  filter: blur(40px);
  opacity: 0.1;
}

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

@keyframes floatTextMobile {
  0%, 100% {
    transform: translateY(-50%) rotate(0deg);
  }
  50% {
    transform: translateY(-55%) rotate(3deg);
  }
}

@keyframes floatText {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
    text-shadow: 0 0 0 rgba(114, 9, 183, 0);
  }
  50% {
    transform: translateY(-12px) rotate(2deg);
    text-shadow: 0 8px 15px rgba(114, 9, 183, 0.25);
  }
}

@keyframes spin {
  0% {
    transform: translateX(-200%) rotate(0deg);
  }
  100% {
    transform: translateX(-200%) rotate(360deg);
  }
}

@media (max-width: 480px) {
  .logo {
    width: 40px;
    left: -50px;
    animation: floatTextMobile 3s ease-in-out infinite;
  }

  .home-container {
    gap: 1.5rem;
  }

  .header {
    margin-bottom: 2.5rem;
    padding-top: 1.5rem;
  }

  .glitch-title {
    font-size: clamp(2.4rem, 8vw, 3.5rem);
  }

  .home-container {
    padding: 1rem 1rem 200px;
  }

  .apps-container {
    grid-template-columns: 1fr;
    margin-bottom: 6rem;
  }

  .el-dropdown-menu {
    background: rgba(255, 255, 255, 0.95) !important;
    backdrop-filter: blur(10px);
    border: 1px solid rgba(67, 97, 238, 0.1);
    box-shadow: 0 4px 12px rgba(67, 97, 238, 0.15);
  }

  .el-dropdown-menu__item {
    color: #4361ee !important;
    padding: 8px 16px;
    transition: all 0.3s ease;
  }

  .el-dropdown-menu__item:hover {
    background: rgba(67, 97, 238, 0.1) !important;
  }
}
</style>
