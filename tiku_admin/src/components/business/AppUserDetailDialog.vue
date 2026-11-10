<script setup>
import AppDialog from '@/components/common/AppDialog.vue'
import UserLink from '@/components/business/UserLink.vue'
import { APP_USER_DETAIL_FIELDS } from '@/constants/app-user'
import { useAppUserDetail } from '@/composables/useAppUserDetail'

const { detailVisible, detailLoading, detailData, closeUserDetail } = useAppUserDetail()

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
    title="用户详情"
    :show-footer="false"
    :loading="detailLoading"
    width="920px"
    @closed="closeUserDetail"
  >
    <template v-if="detailData">
      <el-descriptions :column="2" border>
        <el-descriptions-item
          v-for="field in APP_USER_DETAIL_FIELDS"
          :key="field.key"
          :label="field.label"
          :span="['subjects', 'maOpenId', 'mpOpenId'].includes(field.key) ? 2 : 1"
        >
          <template v-if="field.key === 'userId'">
            <UserLink :user-id="detailData.userId" :label="String(detailData.userId)" />
          </template>
          <template v-else-if="field.key === 'avatar' && detailData.avatar">
            <el-image :src="detailData.avatar" fit="cover" style="width: 72px; height: 72px; border-radius: 8px;" />
          </template>
          <template v-else>
            {{ formatValue(field, detailData) }}
          </template>
        </el-descriptions-item>
      </el-descriptions>
    </template>
  </AppDialog>
</template>
