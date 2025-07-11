<template>
  <div class="auth-container">
    <div class="auth-card">
      <h2>用户登录</h2>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>账号</label>
          <input v-model="form.userAccount" type="text" required>
        </div>
        <div class="form-group">
          <label>密码</label>
          <input v-model="form.userPassword" type="password" required>
        </div>
        <button type="submit" class="auth-button">登录</button>
      </form>
      <p class="auth-link">
        没有账号？
        <router-link to="/register">立即注册</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import {ref} from 'vue';
import {useRouter} from 'vue-router';
import {loginUser} from '../api';
import {useUserStore} from '../stores/user';

const router = useRouter();
const userStore = useUserStore();
const form = ref({
  userAccount: '',
  userPassword: ''
});

const handleLogin = async () => {
  try {
    const {data} = await loginUser(form.value);
    if (data !== null) {
      userStore.login({
        userName: data.data.userName,
        userAvatar: data.data.userAvatar
      });
      await router.push('/');
    }
  } catch (error) {
    console.error('登录失败:', error);
    alert(error.response?.data?.message || '登录失败，请检查账号密码');
  }
};
</script>