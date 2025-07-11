import {createApp} from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import {createHead} from '@vueuse/head'
import 'element-plus/dist/index.css'
import './style.css'
import { createPinia } from 'pinia'

const app = createApp(App)
const head = createHead()
const pinia = createPinia()

app.use(router)
app.use(head)
app.mount('#app')
app.use(ElementPlus)
app.use(pinia)