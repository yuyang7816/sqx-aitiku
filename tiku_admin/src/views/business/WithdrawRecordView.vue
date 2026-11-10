<script setup>
import { ref } from 'vue'
import CrudPage from '@/components/common/CrudPage.vue'
import { confirmWithdrawRecordApi, withdrawRecordApi } from '@/api/modules/withdraw-record'
import {
  USER_FILTER_FIELDS,
  WITHDRAW_ACCOUNT_TYPE_MAP,
  WITHDRAW_ACCOUNT_TYPE_OPTIONS,
  WITHDRAW_RECORD_STATUS_MAP,
  WITHDRAW_RECORD_STATUS_OPTIONS,
  formatEnumColumn,
} from '@/constants/admin-enums'
import { showSuccess } from '@/utils/modal'

const crudRef = ref()

const filterFields = [
  ...USER_FILTER_FIELDS,
  { key: 'status', label: '状态', type: 'select', options: WITHDRAW_RECORD_STATUS_OPTIONS, placeholder: '全部状态' },
]

const columns = [
  { prop: 'userId', label: '用户', minWidth: 120 },
  { prop: 'amount', label: '金额', width: 100 },
  { prop: 'actualAmount', label: '到账金额', width: 100 },
  { prop: 'accountType', label: '账户类型', width: 100, formatter: formatEnumColumn(WITHDRAW_ACCOUNT_TYPE_MAP) },
  { prop: 'status', label: '状态', width: 110, formatter: formatEnumColumn(WITHDRAW_RECORD_STATUS_MAP) },
  { prop: 'createTime', label: '创建时间', minWidth: 180 },
]

const detailFields = [
  { key: 'userName', label: '用户' },
  { key: 'phone', label: '手机号' },
  { key: 'amount', label: '提现金额' },
  { key: 'actualAmount', label: '到账金额' },
  { key: 'serviceFee', label: '手续费' },
  { key: 'accountType', label: '账户类型', formatter: (value) => formatEnumColumn(WITHDRAW_ACCOUNT_TYPE_MAP)(value) },
  { key: 'status', label: '状态', formatter: (value) => formatEnumColumn(WITHDRAW_RECORD_STATUS_MAP)(value) },
  { key: 'reason', label: '拒绝理由' },
  { key: 'withdrawalAccount.accountName', label: '账户名称' },
  { key: 'withdrawalAccount.accountNumber', label: '收款码/账号' },
  { key: 'withdrawalAccount.realName', label: '收款姓名' },
  { key: 'createTime', label: '创建时间' },
  { key: 'withdrawTime', label: '审核/打款时间' },
  { key: 'updateTime', label: '更新时间' },
]

async function confirmRow(row) {
  await confirmWithdrawRecordApi({ id: row.id })
  showSuccess('提现已确认')
  crudRef.value?.loadData()
}
</script>

<template>
  <CrudPage
    ref="crudRef"
    title="提现记录"
    :api="withdrawRecordApi"
    :columns="columns"
    :detail-fields="detailFields"
    :filter-fields="filterFields"
    :show-create="false"
  >
    <template #actions="{ row }">
      <el-button v-if="row.status === 1" link type="success" @click="confirmRow(row)">确认</el-button>
    </template>
  </CrudPage>
</template>
