<template>
  <div class="auth-container">
    <div class="auth-card">
      <h2>用户注册</h2>
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label>账号</label>
          <input v-model="form.userAccount" type="text" required>
        </div>
        <div class="form-group">
          <label>用户名</label>
          <input v-model="form.userName" type="text" required>
        </div>
        <div class="form-group">
          <label>手机号</label>
          <input v-model="form.phoneNumber" type="tel" required>
        </div>
        <div class="form-group">
          <label>密码</label>
          <input v-model="form.userPassword" type="password" required>
        </div>
        <div class="form-group">
          <label>确认密码</label>
          <input v-model="form.checkPassword" type="password" required>
        </div>
        <div class="form-group">
          <label>邮箱</label>
          <input v-model="form.email" type="email" required>
        </div>
        <div class="form-group">
          <label>学历</label>
          <input v-model="form.grade" type="text" >
        </div>
        <div class="form-group">
          <label>工作经验</label>
          <input v-model="form.workExperience" type="text" >
        </div>
        <div class="form-group">
          <label>擅长方向</label>
          <input v-model="form.expertiseDirection" type="text">
        </div>
        <button type="submit" class="auth-button">注册</button>
      </form>
      <p class="auth-link">
        已有账号？
        <router-link to="/login">立即登录</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import {ref} from 'vue';
import {useRouter} from 'vue-router';
import { registerUser } from '../api';

const router = useRouter();
const form = ref({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
  userName: '',
  phoneNumber: '',
  email: '',
  grade: "",
  workExperience: "",
  expertiseDirection: "",
});

const handleRegister = async () => {
  if (form.value.userPassword !== form.value.checkPassword) {
    return alert('两次密码输入不一致');
  }

  try {
    const { data } = await registerUser(form.value);
    if (data.code === 0) {
      alert('注册成功，请登录');
      router.push('/login');
    }
  } catch (error) {
    console.error('注册失败:', error);
    alert(error.response?.data?.message || '注册失败，请检查输入信息');
  }
};
</script>