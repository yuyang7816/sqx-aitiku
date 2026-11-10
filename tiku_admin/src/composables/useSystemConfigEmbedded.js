import { inject } from 'vue'

/** 系统配置 Tab 内嵌模式：隐藏与 Tab 重复的页面标题 */
export function useSystemConfigEmbedded() {
  return inject('systemConfigEmbedded', false)
}
