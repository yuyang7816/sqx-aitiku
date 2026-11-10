import { ref } from 'vue'
import { defineStore } from 'pinia'
import { getDashboardOverview } from '@/api/modules/stats'

/** 侧边栏角标 path 映射（含历史路由别名） */
export const MENU_BADGE_PATHS = {
  messages: '/business/messages',
  promoWithdraw: '/business/promo-withdraw',
  withdrawRecords: '/business/withdraw-records',
  kefuChat: '/business/kefu-chat',
  customerConfig: '/business/customer-config',
}

export const useMenuBadgesStore = defineStore('menuBadges', () => {
  const badges = ref({})
  let loading = false

  async function refresh() {
    if (loading) {
      return
    }
    loading = true
    try {
      const data = await getDashboardOverview()
      const pendingWithdraw = Number(data?.pendingWithdrawCount || 0)
      const kefuUnread = Number(data?.kefuUnreadCount || 0)
      badges.value = {
        [MENU_BADGE_PATHS.messages]: Number(data?.messageUnreadCount || 0),
        [MENU_BADGE_PATHS.promoWithdraw]: pendingWithdraw,
        [MENU_BADGE_PATHS.withdrawRecords]: pendingWithdraw,
        [MENU_BADGE_PATHS.kefuChat]: kefuUnread,
        [MENU_BADGE_PATHS.customerConfig]: kefuUnread,
      }
    } catch {
      badges.value = {}
    } finally {
      loading = false
    }
  }

  function getCount(path) {
    if (!path) {
      return 0
    }
    return Number(badges.value[path] || 0)
  }

  function getSubMenuCount(children = []) {
    return (children || []).reduce((sum, child) => sum + getCount(child.path), 0)
  }

  return {
    badges,
    refresh,
    getCount,
    getSubMenuCount,
  }
})
