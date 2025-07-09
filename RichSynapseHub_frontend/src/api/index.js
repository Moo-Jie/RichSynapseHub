import axios from 'axios'

// 根据环境变量设置 API 基础 URL
const API_BASE_URL = process.env.NODE_ENV === 'production'
    ? '/api'
    : 'http://localhost:8101/api'

// 创建axios实例
const request = axios.create({
    baseURL: API_BASE_URL,
    timeout: 60000
})

// 封装SSE连接
export const connectSSE = (url, params, onMessage, onError) => {
    // 构建带参数的URL
    const queryString = Object.keys(params)
        .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
        .join('&')

    const fullUrl = `${API_BASE_URL}${url}?${queryString}`

    // 创建EventSource
    const eventSource = new EventSource(fullUrl)

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

// AI 面试专家聊天
export const chatWithLoveApp = (message, chatId) => {
    return connectSSE('/doChat/stream', {message, chatId})
}

//  AI 自主规划智能体聊天
export const chatWithManus = (message) => {
    return connectSSE('/doChat/manus/stream', {message})
}

export default {
    chatWithLoveApp,
    chatWithManus
} 