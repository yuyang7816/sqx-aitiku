<script setup>
import { reactive, ref } from 'vue'
import CrudPage from '@/components/common/CrudPage.vue'
import AppDialog from '@/components/common/AppDialog.vue'
import { updateUserWalletAmountApi, userWalletApi } from '@/api/modules/user-wallet'
import { USER_FILTER_FIELDS } from '@/constants/admin-enums'
import { showSuccess } from '@/utils/modal'

const crudRef = ref()
const amountDialogVisible = ref(false)
const amountLoading = ref(false)
const amountForm = reactive({ userId: null, amount: 0, operator: 'incr' })

const filterFields = [...USER_FILTER_FIELDS]

const columns = [
  { prop: 'userId', label: '用户', minWidth: 120 },
  { prop: 'amount', label: '余额', minWidth: 120 },
  { prop: 'createTime', label: '创建时间', minWidth: 180 },
  { prop: 'updateTime', label: '更新时间', minWidth: 180 },
]

const detailFields = [
  { key: 'userName', label: '用户' },
  { key: 'phone', label: '手机号' },
  { key: 'amount', label: '余额' },
  { key: 'createTime', label: '创建时间' },
  { key: 'updateTime', label: '更新时间' },
]

function openAmount(row) {
  Object.assign(amountForm, { userId: row.userId, amount: 0, operator: 'incr' })
  amountDialogVisible.value = true
}

async function submitAmount() {
  amountLoading.value = true
  try {
    await updateUserWalletAmountApi({
      userId: amountForm.userId,
      operator: amountForm.operator,
      amount: amountForm.amount,
    })
    showSuccess('余额已调整')
    amountDialogVisible.value = false
    crudRef.value?.loadData()
  } finally {
    amountLoading.value = false
  }
}
</script>

<template>
  <CrudPage
    ref="crudRef"
    title="用户钱包"
    :api="userWalletApi"
    :columns="columns"
    :detail-fields="detailFields"
    :filter-fields="filterFields"
    :show-create="false"
    :show-edit="false"
    :show-delete="false"
  >
    <template #actions="{ row }">
      <el-button link type="warning" @click="openAmount(row)">调额</el-button>
    </template>
  </CrudPage>

  <AppDialog v-model="amountDialogVisible" title="调整余额" :loading="amountLoading" @confirm="submitAmount">
    <el-form :model="amountForm" label-width="110px">
      <el-form-item label="金额"><el-input-number v-model="amountForm.amount" :min="0" :precision="2" /></el-form-item>
      <el-form-item label="方向">
        <el-select v-model="amountForm.operator">
          <el-option label="增加" value="incr" />
          <el-option label="扣减" value="decr" />
        </el-select>
      </el-form-item>
    </el-form>
  </AppDialog>
</template>
