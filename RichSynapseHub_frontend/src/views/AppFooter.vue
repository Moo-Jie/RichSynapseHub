<template>
  <footer class="app-footer">
    <div class="footer-content">
      <p>{{ copyrightText }}</p>
      <a href="https://github.com/Moo-Jie" target="_blank" class="github-link">
        <svg class="github-icon" viewBox="0 0 24 24">
          <path fill="currentColor"
                d="M12 .297c-6.63 0-12 5.373-12 12 0 5.303 3.438 9.8 8.205 11.385.6.113.793-.258.793-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.815 2.809 1.29 3.495.993.09-.775.42-1.29.765-1.586-2.67-.3-5.466-1.334-5.466-5.93 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.4 3-.405 1.02.005 2.04.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.22 0 1.606-.015 2.896-.015 3.286 0 .315.21.69.825.57C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12"/>
        </svg>
        GitHub
      </a>
    </div>
  </footer>
</template>

<script setup>
import {ref} from 'vue';

const copyrightText = ref(' ©2025 RichSynapseHub | 一个实用的 AI 智能体应用集');
</script>

<style scoped>
/* 新增GitHub链接样式 */
.github-link {
  display: inline-flex;
  align-items: center;
  margin-left: 1.5rem;
  padding: 0.6rem 1.5rem;
  background: rgba(0, 0, 0, 0.85);
  color: white;
  border-radius: 100px;
  text-decoration: none;
  transition: all 0.4s cubic-bezier(0.2, 0.8, 0.4, 1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.github-link:hover {
  background: rgba(0, 0, 0, 0.95);
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
}

.github-icon {
  width: 1.2rem;
  height: 1.2rem;
  margin-right: 0.8rem;
}

.app-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  z-index: 100;
  padding: 1.5rem 0; /* 原为2rem */
  text-align: center;
  color: #6c757d;
  font-size: 0.9rem;
  overflow: hidden;
  background: linear-gradient(160deg, rgba(248, 249, 250, 0.95) 0%, rgba(255, 255, 255, 0.92) 100%);
  box-shadow: 0 -8px 25px rgba(0, 0, 0, 0.08);
}

/* 增强型波浪效果 */
.waves {
  position: absolute;
  top: -15px;
  left: 0;
  width: 100%;
  height: 50px;
}

.wave {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-repeat: repeat-x;
  background-size: 1200px 50px;
  animation: waveAnimation 12s infinite linear;
}

.wave-1 {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1200 50' preserveAspectRatio='none'%3E%3Cpath fill='rgba(255,255,255,0.95)' d='M0,25 C150,50 350,0 600,25 C850,50 1050,0 1200,25 L1200,50 L0,50 Z'%3E%3C/path%3E%3C/svg%3E");
  opacity: 0.9;
  animation-delay: 0s;
}

.wave-2 {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 1200 50' preserveAspectRatio='none'%3E%3Cpath fill='rgba(255,255,255,0.85)' d='M0,35 C200,0 400,40 600,35 C800,40 1000,0 1200,35 L1200,50 L0,50 Z'%3E%3C/path%3E%3C/svg%3E");
  animation-delay: -6s;
  animation-duration: 16s;
  opacity: 0.7;
}

/* 版权文本容器 */
.footer-content {
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 优化后的文字效果 */
.app-footer p {
  margin: 0;
  position: relative;
  display: inline-block;
  padding: 0.6rem 2rem;
  background: rgba(255, 255, 255, 0.85);
  border-radius: 100px;
  backdrop-filter: blur(6px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.07),
  0 2px 0 rgba(255, 255, 255, 0.7) inset;
  transition: transform 0.4s cubic-bezier(0.2, 0.8, 0.4, 1),
  box-shadow 0.4s ease;
  font-weight: 500;
  color: #495057;
}

.app-footer p:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12),
  0 2px 0 rgba(255, 255, 255, 0.8) inset;
  background: rgba(255, 255, 255, 0.92);
}

.app-footer p::before {
  content: '';
  position: absolute;
  top: -4px;
  left: 10%;
  width: 80%;
  height: 2px;
  background: linear-gradient(90deg, rgba(0, 0, 0, 0), #4cc9f0, rgba(0, 0, 0, 0));
  opacity: 0.5;
  transition: opacity 0.3s ease;
}

.app-footer p:hover::before {
  opacity: 0.8;
}

/* 动画效果增强 */
@keyframes waveAnimation {
  0% {
    background-position-x: 0;
  }
  100% {
    background-position-x: 1200px;
  }
}

/* 响应式设计优化 */
@media (max-width: 768px) {
  .app-footer {
    padding: 1.5rem 0;
  }

  .app-footer p {
    padding: 0.5rem 1.5rem;
    font-size: 0.85rem;
  }

  .footer-content {
    flex-direction: column;
    gap: 1rem;
  }

  .github-link {
    margin-left: 0;
  }

}

@media (max-width: 480px) {
  .app-footer {
    padding: 1.2rem 0;
  }

  .waves {
    height: 30px;
    top: -7px;
  }

  .app-footer p {
    padding: 0.4rem 1.2rem;
    font-size: 0.8rem;
    border-radius: 18px;
  }
}
</style>