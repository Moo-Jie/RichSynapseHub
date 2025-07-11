import axios from 'axios';
import {useUserStore} from '../stores/user';

// 根据环境变量设置 API 基础 URL
const API_BASE_URL = process.env.NODE_ENV === 'production'
    ? '/api'
    : 'http://localhost:8101/api'

// 创建axios实例
const request = axios.create({
    baseURL: API_BASE_URL,
    timeout: 60000,
    withCredentials: true
})

// 请求拦截器
request.interceptors.request.use(config => {
    return config;
});

// 封装SSE连接
export const connectSSE = (url, params, onMessage, onError) => {
    const fullUrl = `${API_BASE_URL}${url}`;

    const eventSource = new EventSource(fullUrl, {
        withCredentials: true
    });


    eventSource.onmessage = event => {
        let data = event.data

        // 检查是否是特殊标记
        if (data === '[DONE]') {
            if (onMessage) onMessage('[DONE]')
        } else {
            // 处理普通消息
            if (onMessage) onMessage(data)
        }
    }

    eventSource.onerror = error => {
        if (onError) onError(error)
        eventSource.close()
    }

    // 返回eventSource实例，以便后续可以关闭连接
    return eventSource
}

export const chatWithInterviewApp = (message, chatId) => {
    return connectSSE('/doChat/stream', {message, chatId})
}

export const chatWithManus = (message) => {
    return connectSSE('/doChat/manus/stream', {message})
}

export const loginUser = (data) => {
    return request.post('/user/login', data)
}

export const registerUser = (data) => {
    return request.post('/user/register', data)
}

export default request