<template>
  <div
      class="app-card"
      :class="{'clicked': isClicked}"
      @click="handleClick"
  >
    <div class="card-top">
      <div class="app-icon" :class="themeClass">{{ icon }}</div>
      <div class="app-info">
        <div class="app-title">{{ title }}</div>
        <div class="app-desc">{{ description }}</div>
      </div>
    </div>
    <div class="app-button">
      <span class="btn-text">立即体验</span>
      <span class="btn-icon">→</span>
    </div>
  </div>
</template>

<script setup>
import {ref} from 'vue';

const props = defineProps({
  icon: {
    type: String,
    required: true
  },
  title: {
    type: String,
    required: true
  },
  description: {
    type: String,
    required: true
  }
});

const emit = defineEmits(['click']);

const isClicked = ref(false);

const handleClick = (event) => {
  isClicked.value = true;
  setTimeout(() => {
    isClicked.value = false;
    emit('click');
  }, 300);
}
</script>

<style scoped>
/* 设计变量 */
:root {
  --primary: #4361ee;
  --primary-light: #4cc9f0;
  --secondary: #f72585;
  --accent: #7209b7;
  --light: #f8f9fa;
  --white: #ffffff;
  --shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  --transition: all 0.4s cubic-bezier(0.2, 0.8, 0.4, 1);
  --border-radius: 16px;
}

.app-card {
  background: var(--white);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow);
  padding: 2rem;
  position: relative;
  overflow: hidden;
  transform: translateY(0);
  transition: var(--transition);
  cursor: pointer;
  z-index: 1;
}

.app-card.clicked {
  transform: scale(0.97);
  opacity: 0.9;
}

.app-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, var(--primary-light), var(--secondary));
  transform: scaleX(0);
  transform-origin: left;
  transition: var(--transition);
  z-index: -1;
}

.app-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
}

.app-card:hover::before {
  transform: scaleX(1);
}

.card-top {
  display: flex;
  align-items: center;
  margin-bottom: 1.5rem;
}

.app-icon {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 1.25rem;
  font-size: 1.8rem;
  flex-shrink: 0;
  transition: transform 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.interView-icon {
  background: linear-gradient(135deg, #ff758c, #ff7eb3);
  color: var(--white);
  box-shadow: 0 4px 12px rgba(255, 117, 140, 0.3);
}

.robot-icon {
  background: linear-gradient(135deg, var(--primary), var(--accent));
  color: var(--white);
  box-shadow: 0 4px 12px rgba(67, 97, 238, 0.3);
}

.app-card:hover .app-icon {
  transform: scale(1.12) rotate(8deg);
}

.app-info {
  flex: 1;
  text-align: left;
}

.app-title {
  font-family: 'Manrope', sans-serif;
  font-weight: 700;
  font-size: 1.5rem;
  margin-bottom: 0.25rem;
  color: var(--primary);
  text-shadow: 0 2px 4px rgba(67, 97, 238, 0.1);
}

.app-desc {
  font-size: 0.9rem;
  color: #6c757d;
  line-height: 1.6;
  transition: color 0.3s ease;
}

.app-card:hover .app-desc {
  color: #495057;
}

.app-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.8rem 1.8rem;
  background: var(--white);
  color: var(--primary);
  border-radius: 50px;
  font-weight: 600;
  text-decoration: none;
  border: 2px solid var(--primary);
  transition: var(--transition);
  margin-top: 1rem;
  position: relative;
  overflow: hidden;
}

.app-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 0%;
  background: linear-gradient(to right, var(--primary), var(--accent));
  transition: var(--transition);
  z-index: -1;
}

.app-card:hover .app-button::before {
  height: 100%;
}

.app-button:hover {
  color: var(--white);
  transform: translateY(-3px);
  box-shadow: 0 6px 15px rgba(67, 97, 238, 0.3);
}

.btn-icon {
  margin-left: 0.5rem;
  transition: var(--transition);
}

.app-button:hover .btn-icon {
  transform: translateX(6px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-card {
    padding: 1.75rem;
  }
}

@media (max-width: 480px) {
  .card-top {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .app-icon {
    margin-right: 0;
    margin-bottom: 1rem;
  }

  .app-info {
    text-align: center;
  }

  .app-button {
    width: 100%;
  }
}
</style>
