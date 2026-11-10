<script setup>
import { computed } from 'vue'
import AppDialog from '@/components/common/AppDialog.vue'
import UserLink from '@/components/business/UserLink.vue'
import OrderLink from '@/components/business/OrderLink.vue'
import { resolveUserDisplayName, resolveUserPhone } from '@/utils/user-display'
import { isImageField, resolveImageUrl } from '@/utils/image-field'
import {
  getWithdrawalAccountNumber,
  shouldShowWechatQrImage,
} from '@/utils/withdrawal-account'
import {
  buildRecordDetailItems,
  getNestedValue,
  isOrderLinkField,
  isUserLinkField,
  resolveLinkedOrderId,
  resolveLinkedUserId,
  resolveRecordFieldValue,
} from '@/utils/record-detail'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: '详情' },
  loading: { type: Boolean, default: false },
  data: { type: Object, default: null },
  fields: { type: Array, default: () => [] },
  width: { type: String, default: '920px' },
})

const emit = defineEmits(['update:modelValue'])

const detailItems = computed(() => buildRecordDetailItems(props.data || {}, props.fields))

function resolveUserLinkLabel(field, data) {
  if (field.key === 'userId') {
    return resolveUserDisplayName(data)
  }
  if (field.key === 'phone' || field.key.endsWith('.phone')) {
    return String(data.phone || data.userEntity?.phone || resolveRecordFieldValue(field, data))
  }
  return resolveUserDisplayName(data) || String(resolveLinkedUserId(data) || '—')
}

function shouldRenderImage(field, data) {
  if (shouldShowWechatQrImage(data, field.key)) {
    return true
  }
  const raw = getNestedValue(data, field.key)
  return isImageField(field, raw)
}

function resolveDetailImageUrl(field, data) {
  if (shouldShowWechatQrImage(data, field.key)) {
    return resolveImageUrl(getWithdrawalAccountNumber(data, field.key))
  }
  return resolveImageUrl(getNestedValue(data, field.key))
}
</script>

<template>
  <AppDialog
    :model-value="modelValue"
    :title="title"
    :show-footer="false"
    :loading="loading"
    :width="width"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <template v-if="data">
      <el-descriptions :column="2" border>
        <el-descriptions-item
          v-for="field in detailItems"
          :key="field.key"
          :label="field.label"
          :span="['description', 'content', 'reason', 'title', 'extraData'].includes(field.key) ? 2 : 1"
        >
          <UserLink
            v-if="isUserLinkField(field.key) && resolveLinkedUserId(data)"
            :user-id="resolveLinkedUserId(data)"
            :label="resolveUserLinkLabel(field, data)"
            :phone="field.key === 'phone' || field.key.endsWith('.phone') ? resolveUserPhone(data) : ''"
          />
          <OrderLink
            v-else-if="isOrderLinkField(field.key) && resolveLinkedOrderId(data, field.key)"
            :order-id="resolveLinkedOrderId(data, field.key)"
            :label="field.key === 'orderNo'
              ? String(data.orderNo || resolveLinkedOrderId(data, field.key))
              : String(resolveLinkedOrderId(data, field.key))"
          />
          <div v-else-if="shouldRenderImage(field, data)" class="detail-image-wrap">
            <el-image
              :src="resolveDetailImageUrl(field, data)"
              fit="contain"
              :preview-src-list="[resolveDetailImageUrl(field, data)]"
              style="width: 180px; height: 180px"
              preview-teleported
            />
          </div>
          <template v-else>
            {{ resolveRecordFieldValue(field, data) }}
          </template>
        </el-descriptions-item>
      </el-descriptions>
    </template>
  </AppDialog>
</template>

<style scoped>
.detail-image-wrap {
  padding: 4px 0;
}
</style>
