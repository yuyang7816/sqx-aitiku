import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import App from './App.vue'
import router from './router'
import pinia from './stores'
import { useThemeStore } from './stores/theme'
import '@/styles/index.scss'

const app = createApp(App)

app.use(pinia)
useThemeStore(pinia).initTheme()
app.use(router).use(ElementPlus, { locale: zhCn }).mount('#app')
