<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppDialog from '@/components/common/AppDialog.vue'
import BasePagination from '@/components/common/BasePagination.vue'
import TableRowActions from '@/components/common/TableRowActions.vue'
import UserLink from '@/components/business/UserLink.vue'
import WithdrawAccountNumberCell from '@/components/business/WithdrawAccountNumberCell.vue'
import RecordDetailDialog from '@/components/common/RecordDetailDialog.vue'
import {
  approveWithdrawRecordApi,
  rejectWithdrawRecordApi,
  withdrawRecordApi,
} from '@/api/modules/withdraw-record'
import {
  WITHDRAW_ACCOUNT_TYPE_OPTIONS,
  WITHDRAW_STATUS_OPTIONS,
  getWithdrawAccountTypeLabel,
  getWithdrawStatusLabel,
  getWithdrawStatusTagType,
  maskPhone,
} from '@/constants/promo'
import { useFilterFormKey } from '@/composables/useFilterFormKey'
import { useMenuBadgesStore } from '@/stores/menu-badges'
import FilterSelect from '@/components/common/FilterSelect.vue'
import FilterSummary from '@/components/common/FilterSummary.vue'
import { normalizeNumberFilterQuery } from '@/utils/filter-form'
import { validateForm } from '@/utils/form'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { buildSystemConfigRoute } from '@/constants/system-config-tabs'

const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const rejectVisible = ref(false)
const rejectLoading = ref(false)
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref(null)
const rejectFormRef = ref()
const rejectTarget = ref(null)
const rejectForm = reactive({ reason: '' })
const menuBadgesStore = useMenuBadgesStore()
const { filterFormKey, bumpFilterFormKey } = useFilterFormKey()

const FILTER_FIELDS = [
  { key: 'phone', label: '申请人手机号', type: 'text', placeholder: '请输入手机号' },
  { key: 'userName', label: '申请人用户名', type: 'text', placeholder: '请输入用户名' },
  { key: 'status', label: '审核状态', type: 'select', options: WITHDRAW_STATUS_OPTIONS, placeholder: '全部状态' },
]

const filters = reactive({
  phone: '',
  userName: '',
  status: 1,
})

const rejectRules = {
  reason: [{ required: true, message: '请填写拒绝理由', trigger: 'blur' }],
}

function buildQueryParams() {
  return {
    page: pageNum.value,
    limit: pageSize.value,
    phone: filters.phone || undefined,
    userName: filters.userName || undefined,
    status: normalizeNumberFilterQuery(filters.status),
  }
}

function resolveUserName(row) {
  return row?.userEntity?.nickname || row?.userEntity?.userName || '-'
}

function resolveUserPhone(row) {
  return row?.userEntity?.phone || '-'
}

function resolveAccountInfo(row) {
  const account = row?.withdrawalAccount
  if (!account) return '-'
  const channel = getWithdrawAccountTypeLabel(row.accountType)
  const name = account.accountName || account.realName || ''
  if (Number(account.type) === 2 && account.accountNumber) {
    return [channel, name].filter(Boolean).join(' / ') || channel
  }
  const number = account.accountNumber || ''
  return [channel, name, number].filter(Boolean).join(' / ')
}

async function loadData() {
  loading.value = true
  try {
    const data = await withdrawRecordApi.page(buildQueryParams())
    tableData.value = data.list
    total.value = data.total
  } catch (error) {
    showError(error?.msg || error?.message || '加载提现记录失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

function handleReset() {
  Object.assign(filters, { phone: '', userName: '', status: 1 })
  bumpFilterFormKey()
  pageNum.value = 1
  loadData()
}

async function handleApprove(row) {
  await confirmAction(`确认通过用户「${resolveUserName(row)}」的 ¥${row.amount} 提现申请吗？`)
  try {
    await approveWithdrawRecordApi({ id: row.id })
    showSuccess('提现已审核通过')
    await loadData()
    void menuBadgesStore.refresh()
  } catch (error) {
    showError(error?.msg || error?.message || '审核失败')
  }
}

async function openDetail(row) {
  detailVisible.value = true
  detailLoading.value = true
  detailData.value = null
  try {
    detailData.value = await withdrawRecordApi.info(row.id)
  } catch (error) {
    showError(error?.msg || error?.message || '加载提现详情失败')
    detailVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

function openReject(row) {
  rejectTarget.value = row
  rejectForm.reason = ''
  rejectVisible.value = true
}

async function submitReject() {
  const valid = await validateForm(rejectFormRef)
  if (!valid || !rejectTarget.value) return
  rejectLoading.value = true
  try {
    await rejectWithdrawRecordApi({ id: rejectTarget.value.id, reason: rejectForm.reason })
    showSuccess('已拒绝提现，余额已退回用户钱包')
    rejectVisible.value = false
    await loadData()
    void menuBadgesStore.refresh()
  } catch (error) {
    showError(error?.msg || error?.message || '拒绝失败')
  } finally {
    rejectLoading.value = false
  }
}

function goWithdrawConfig() {
  router.push(buildSystemConfigRoute('withdraw'))
}

onMounted(loadData)
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <strong>提现审核</strong>
        <el-button type="primary" link @click="goWithdrawConfig">提现规则配置</el-button>
      </div>
    </template>

    <p class="page-desc">
      审核用户发起的提现申请。通过后标记为已打款；拒绝后金额将自动退回用户钱包。手续费、最低/最高提现金额请在
      <el-link type="primary" :underline="false" @click="goWithdrawConfig">系统配置 · 提现配置</el-link>
      中维护。
    </p>

    <el-form :key="filterFormKey" :inline="true" class="filter-form" @submit.prevent="handleSearch">
      <el-form-item label="申请人手机号">
        <el-input v-model="filters.phone" clearable placeholder="请输入手机号" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="申请人用户名">
        <el-input v-model="filters.userName" clearable placeholder="请输入用户名" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="审核状态">
        <FilterSelect v-model="filters.status" clearable placeholder="全部状态">
          <el-option
            v-for="item in WITHDRAW_STATUS_OPTIONS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </FilterSelect>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
    <FilterSummary :filters="filters" :fields="FILTER_FIELDS" />

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column label="申请人" min-width="120">
        <template #default="{ row }">
          <UserLink
            :user-id="row.userId"
            :label="resolveUserName(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="手机号" width="130">
        <template #default="{ row }">
          <UserLink :user-id="row.userId" :phone="maskPhone(resolveUserPhone(row))" />
        </template>
      </el-table-column>
      <el-table-column label="申请金额" width="110">
        <template #default="{ row }">¥{{ row.amount ?? '-' }}</template>
      </el-table-column>
      <el-table-column label="手续费" width="100">
        <template #default="{ row }">¥{{ row.serviceFee ?? '0' }}</template>
      </el-table-column>
      <el-table-column label="到账金额" width="110">
        <template #default="{ row }">¥{{ row.actualAmount ?? '-' }}</template>
      </el-table-column>
      <el-table-column label="提现渠道" width="100">
        <template #default="{ row }">{{ getWithdrawAccountTypeLabel(row.accountType) }}</template>
      </el-table-column>
      <el-table-column label="收款账户" min-width="180">
        <template #default="{ row }">
          <div class="account-cell">
            <WithdrawAccountNumberCell
              v-if="row?.withdrawalAccount?.accountNumber && Number(row?.withdrawalAccount?.type) === 2"
              :row="row"
              field-key="withdrawalAccount.accountNumber"
              compact
            />
            <span v-else class="account-text">{{ resolveAccountInfo(row) }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getWithdrawStatusTagType(row.status)">
            {{ getWithdrawStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="申请时间" min-width="170" />
      <el-table-column prop="withdrawTime" label="审核/打款时间" min-width="170">
        <template #default="{ row }">{{ row.withdrawTime || '-' }}</template>
      </el-table-column>
      <el-table-column prop="reason" label="拒绝理由" min-width="140" show-overflow-tooltip>
        <template #default="{ row }">{{ row.reason || '-' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <TableRowActions>
            <el-button link type="primary" @click="openDetail(row)">查看详情</el-button>
            <template v-if="row.status === 1">
              <el-button link type="success" @click="handleApprove(row)">通过</el-button>
              <el-button link type="danger" @click="openReject(row)">拒绝</el-button>
            </template>
            <span v-else class="text-muted">已处理</span>
          </TableRowActions>
        </template>
      </el-table-column>
    </el-table>

    <BasePagination
      :current-page="pageNum"
      :page-size="pageSize"
      :total="total"
      @update:current-page="pageNum = $event; loadData()"
      @update:page-size="pageSize = $event; pageNum = 1; loadData()"
    />
  </el-card>

  <AppDialog
    v-model="rejectVisible"
    title="拒绝提现"
    :loading="rejectLoading"
    @confirm="submitReject"
  >
    <el-form ref="rejectFormRef" :model="rejectForm" :rules="rejectRules" label-width="90px">
      <el-form-item label="拒绝理由" prop="reason">
        <el-input
          v-model="rejectForm.reason"
          type="textarea"
          :rows="4"
          placeholder="将展示给用户，并触发余额退回"
        />
      </el-form-item>
    </el-form>
  </AppDialog>

  <RecordDetailDialog
    v-model="detailVisible"
    title="提现申请详情"
    :loading="detailLoading"
    :data="detailData"
    :fields="[
      { key: 'id', label: '记录 ID' },
      { key: 'userId', label: '用户 ID' },
      { key: 'amount', label: '申请金额' },
      { key: 'serviceFee', label: '手续费' },
      { key: 'actualAmount', label: '到账金额' },
      { key: 'accountType', label: '提现渠道', formatter: (value) => getWithdrawAccountTypeLabel(value) },
      { key: 'status', label: '状态', formatter: (value) => getWithdrawStatusLabel(value) },
      { key: 'reason', label: '拒绝理由' },
      { key: 'createTime', label: '申请时间' },
      { key: 'withdrawTime', label: '审核/打款时间' },
      { key: 'updateTime', label: '更新时间' },
      { key: 'withdrawalAccount.accountName', label: '账户名称' },
      { key: 'withdrawalAccount.accountNumber', label: '收款码/账号' },
      { key: 'withdrawalAccount.realName', label: '收款姓名' },
    ]"
  />
</template>

<style scoped lang="scss">
.page-desc {
  margin: 0 0 16px;
  color: var(--el-text-color-secondary);
  line-height: 1.6;
}

.text-muted {
  color: var(--el-text-color-secondary);
}

.account-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.account-text {
  line-height: 1.5;
  word-break: break-all;
}
</style>
