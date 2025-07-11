import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('token') || '')
    const userInfo = ref(JSON.parse(localStorage.getItem('userInfo')) || null)

    const login = (newToken, userData) => {
        token.value = newToken;
        userInfo.value = {
            id: userData.id,
            username: userData.userName,
            avatar: userData.userAvatar
        }
        localStorage.setItem('satoken', newToken);
        localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    }

    const logout = () => {
        token.value = ''
        userInfo.value = null
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
    }

    return { token, userInfo, login, logout }
})