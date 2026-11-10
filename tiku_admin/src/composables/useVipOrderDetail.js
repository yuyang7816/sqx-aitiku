import { ref } from 'vue'
import { getVipOrderInfo } from '@/api/modules/vip-order'
import { showError } from '@/utils/modal'

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref(null)

export function useVipOrderDetail() {
  async function openOrderDetail(orderId) {
    if (!orderId) return
    detailVisible.value = true
    detailLoading.value = true
    detailData.value = null
    try {
      detailData.value = await getVipOrderInfo(orderId)
    } catch (error) {
      showError(error?.msg || error?.message || '加载订单详情失败')
      detailVisible.value = false
    } finally {
      detailLoading.value = false
    }
  }

  function closeOrderDetail() {
    detailVisible.value = false
  }

  return {
    detailVisible,
    detailLoading,
    detailData,
    openOrderDetail,
    closeOrderDetail,
  }
}
