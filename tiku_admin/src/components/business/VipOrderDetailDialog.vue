<script setup>
import AppDialog from '@/components/common/AppDialog.vue'
import UserLink from '@/components/business/UserLink.vue'
import { VIP_ORDER_DETAIL_FIELDS, getVipOrderPayStatusLabel, getVipOrderPayStatusTagType } from '@/constants/vip-order'
import { useVipOrderDetail } from '@/composables/useVipOrderDetail'

const { detailVisible, detailLoading, detailData, closeOrderDetail } = useVipOrderDetail()

function formatValue(field, row) {
  const raw = row?.[field.key]
  if (field.formatter) {
    return field.formatter(raw, row)
  }
  if (raw === null || raw === undefined || raw === '') {
    return '—'
  }
  return raw
}
</script>

<template>
  <AppDialog
    v-model="detailVisible"
    title="会员订单详情"
    :show-footer="false"
    :loading="detailLoading"
    width="920px"
    @closed="closeOrderDetail"
  >
    <template v-if="detailData">
      <el-descriptions :column="2" border>
        <el-descriptions-item
          v-for="field in VIP_ORDER_DETAIL_FIELDS"
          :key="field.key"
          :label="field.label"
          :span="['tradeNo', 'orderNo'].includes(field.key) ? 2 : 1"
        >
          <template v-if="field.key === 'userId'">
            <UserLink
              :user-id="detailData.userId"
              :label="detailData.nickname || detailData.userName || `用户${detailData.userId}`"
            />
          </template>
          <template v-else-if="field.key === 'payStatus'">
            <el-tag :type="getVipOrderPayStatusTagType(detailData.payStatus)">
              {{ getVipOrderPayStatusLabel(detailData.payStatus) }}
            </el-tag>
          </template>
          <template v-else>
            {{ formatValue(field, detailData) }}
          </template>
        </el-descriptions-item>
      </el-descriptions>
    </template>
  </AppDialog>
</template>
