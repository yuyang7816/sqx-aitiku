<script setup>
import CrudPage from '@/components/common/CrudPage.vue'
import { smsTemplateApi } from '@/api/modules/sms-template'

const filterFields = [
  { key: 'bizCode', label: '业务标识', type: 'text', placeholder: '如 login / register' },
  { key: 'content', label: '模板内容', type: 'text', placeholder: '模糊匹配模板内容' },
]

const columns = [
  { prop: 'bizCode', label: '业务标识', minWidth: 140 },
  { prop: 'content', label: '模板内容', minWidth: 260, showOverflowTooltip: true },
  { prop: 'createTime', label: '创建时间', minWidth: 180 },
]

const detailFields = [
  { key: 'bizCode', label: '业务标识' },
  { key: 'content', label: '模板内容' },
  { key: 'createTime', label: '创建时间' },
  { key: 'updateTime', label: '更新时间' },
]
</script>

<template>
  <CrudPage
    title="短信模板"
    :api="smsTemplateApi"
    :columns="columns"
    :detail-fields="detailFields"
    :filter-fields="filterFields"
  >
    <template #form="{ form }">
      <el-form-item label="业务标识"><el-input v-model="form.bizCode" placeholder="如 login / register" /></el-form-item>
      <el-form-item label="模板内容"><el-input v-model="form.content" type="textarea" :rows="4" /></el-form-item>
    </template>
  </CrudPage>
</template>
