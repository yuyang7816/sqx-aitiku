<script setup>
import CrudPage from '@/components/common/CrudPage.vue'
import { withdrawalAccountApi } from '@/api/modules/withdrawal-account'
import {
  USER_FILTER_FIELDS,
  WITHDRAWAL_ACCOUNT_TYPE_MAP,
  WITHDRAWAL_ACCOUNT_TYPE_OPTIONS,
  formatEnumColumn,
} from '@/constants/admin-enums'

const filterFields = [
  ...USER_FILTER_FIELDS,
  { key: 'accountName', label: '账户名称', type: 'text', placeholder: '模糊匹配账户名称' },
  { key: 'type', label: '类型', type: 'select', options: WITHDRAWAL_ACCOUNT_TYPE_OPTIONS, placeholder: '全部类型' },
]

const columns = [
  { prop: 'userId', label: '用户', minWidth: 120 },
  { prop: 'accountName', label: '账户名称', minWidth: 140 },
  {
    prop: 'accountNumber',
    label: '账号/收款码',
    type: 'withdrawAccountNumber',
    minWidth: 140,
  },
  { prop: 'type', label: '类型', width: 100, formatter: formatEnumColumn(WITHDRAWAL_ACCOUNT_TYPE_MAP) },
  { prop: 'createTime', label: '创建时间', minWidth: 180 },
]

const detailFields = [
  { key: 'userName', label: '用户' },
  { key: 'phone', label: '手机号' },
  { key: 'accountName', label: '账户名称' },
  { key: 'accountNumber', label: '收款码/账号' },
  { key: 'type', label: '类型', formatter: (value) => formatEnumColumn(WITHDRAWAL_ACCOUNT_TYPE_MAP)(value) },
  { key: 'realName', label: '真实姓名' },
  { key: 'createTime', label: '创建时间' },
  { key: 'updateTime', label: '更新时间' },
]
</script>

<template>
  <CrudPage
    title="提现账户"
    :api="withdrawalAccountApi"
    :columns="columns"
    :detail-fields="detailFields"
    :filter-fields="filterFields"
    :default-query="{ source: 1 }"
    :show-create="false"
  />
</template>
