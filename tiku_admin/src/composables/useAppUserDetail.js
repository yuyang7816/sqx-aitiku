import { ref } from 'vue'
import { getAppUserInfo } from '@/api/modules/app-user'
import { ensureGlobalCommissionDefaults } from '@/api/modules/promo-config'
import { showError } from '@/utils/modal'

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref(null)

export function useAppUserDetail() {
  async function openUserDetail(userId) {
    if (!userId) return
    detailVisible.value = true
    detailLoading.value = true
    detailData.value = null
    try {
      await ensureGlobalCommissionDefaults()
      detailData.value = await getAppUserInfo(userId)
    } catch (error) {
      showError(error?.msg || error?.message || '加载用户详情失败')
      detailVisible.value = false
    } finally {
      detailLoading.value = false
    }
  }

  function closeUserDetail() {
    detailVisible.value = false
  }

  return {
    detailVisible,
    detailLoading,
    detailData,
    openUserDetail,
    closeUserDetail,
  }
}
