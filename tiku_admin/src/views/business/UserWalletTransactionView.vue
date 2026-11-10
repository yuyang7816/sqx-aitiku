<script setup>
import CrudPage from '@/components/common/CrudPage.vue'
import { userWalletTransactionApi } from '@/api/modules/user-wallet-transaction'
import {
  USER_FILTER_FIELDS,
  WALLET_BIZ_TYPE_MAP,
  WALLET_DIRECTION_MAP,
  WALLET_DIRECTION_OPTIONS,
  WALLET_TRANSACTION_STATUS_MAP,
  WALLET_TRANSACTION_STATUS_OPTIONS,
  WALLET_TRANSACTION_TYPE_MAP,
  WALLET_TRANSACTION_TYPE_OPTIONS,
  formatEnumColumn,
} from '@/constants/admin-enums'

const filterFields = [
  ...USER_FILTER_FIELDS,
  { key: 'title', label: '标题', type: 'text', placeholder: '模糊匹配标题' },
  { key: 'transactionType', label: '交易类型', type: 'select', options: WALLET_TRANSACTION_TYPE_OPTIONS, placeholder: '全部类型' },
  { key: 'direction', label: '方向', type: 'select', options: WALLET_DIRECTION_OPTIONS, placeholder: '全部方向' },
  { key: 'status', label: '状态', type: 'select', options: WALLET_TRANSACTION_STATUS_OPTIONS, placeholder: '全部状态' },
]

const columns = [
  { prop: 'userId', label: '用户', minWidth: 120 },
  { prop: 'title', label: '标题', minWidth: 160, showOverflowTooltip: true },
  { prop: 'amount', label: '金额', width: 100 },
  { prop: 'transactionType', label: '交易类型', width: 110, formatter: formatEnumColumn(WALLET_TRANSACTION_TYPE_MAP) },
  { prop: 'direction', label: '方向', width: 90, formatter: formatEnumColumn(WALLET_DIRECTION_MAP) },
  { prop: 'status', label: '状态', width: 100, formatter: formatEnumColumn(WALLET_TRANSACTION_STATUS_MAP) },
  { prop: 'createTime', label: '创建时间', minWidth: 180 },
]

const detailFields = [
  { key: 'userName', label: '用户' },
  { key: 'phone', label: '手机号' },
  { key: 'title', label: '标题' },
  { key: 'amount', label: '金额' },
  { key: 'transactionType', label: '交易类型', formatter: (value) => formatEnumColumn(WALLET_TRANSACTION_TYPE_MAP)(value) },
  { key: 'direction', label: '方向', formatter: (value) => formatEnumColumn(WALLET_DIRECTION_MAP)(value) },
  { key: 'bizType', label: '业务类型', formatter: (value) => formatEnumColumn(WALLET_BIZ_TYPE_MAP)(value) },
  { key: 'status', label: '状态', formatter: (value) => formatEnumColumn(WALLET_TRANSACTION_STATUS_MAP)(value) },
  { key: 'description', label: '描述' },
  { key: 'createTime', label: '创建时间' },
  { key: 'updateTime', label: '更新时间' },
]
</script>

<template>
  <CrudPage
    title="钱包流水"
    :api="userWalletTransactionApi"
    :columns="columns"
    :detail-fields="detailFields"
    :filter-fields="filterFields"
    :show-create="false"
  />
</template>
