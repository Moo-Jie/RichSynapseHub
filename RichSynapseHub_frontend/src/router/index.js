import {createRouter, createWebHistory} from 'vue-router'

const routes = [
    {
        path: '/',
        name: 'Home',
        component: () => import('../views/Home.vue'),
        meta: {
            title: '首页 -  RichSynapseHub ',
            description: ' RichSynapseHub 提供 AI 面试专家和 AI 自主规划智能体服务，满足您的各种AI对话需求'
        }
    },
    {
        path: '/interView-master',
        name: 'InterviewMaster',
        component: () => import('../views/InterviewMaster.vue'),
        meta: {
            title: 'AI 面试专家 -  RichSynapseHub ',
            description: 'AI 面试专家是  RichSynapseHub  的专业面试顾问，帮你解答各种面试问题，提供面试建议'
        }
    },
    {
        path: '/super-agent',
        name: 'SuperAgent',
        component: () => import('../views/SelfPlanningAgent.vue'),
        meta: {
            title: ' AI 自主规划智能体 -  RichSynapseHub ',
            description: ' AI 自主规划智能体是 RichSynapseHub 的全能助手，能解答各类专业问题，提供精准建议和解决方案'
        }
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/Login.vue'),
        meta: {
            title: '用户登录 - RichSynapseHub'
        }
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('../views/Register.vue'),
        meta: {
            title: '用户注册 - RichSynapseHub'
        }
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 全局导航守卫，设置文档标题
router.beforeEach((to, from, next) => {
    // 设置页面标题
    if (to.meta.title) {
        document.title = to.meta.title
    }
    next()
})

export default router 