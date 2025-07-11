import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
    const userInfo = ref(JSON.parse(localStorage.getItem('userInfo')) || null)

    const login = ( userData) => {
        userInfo.value = {
            id: userData.id,
            username: userData.userName,
            avatar: userData.userAvatar
        }
        localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    }

    const logout = () => {
        userInfo.value = null
        localStorage.removeItem('userInfo')
    }

    return {  userInfo, login, logout }
})