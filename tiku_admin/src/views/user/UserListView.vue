<script setup>
import { onMounted, reactive, ref, computed, toRef } from 'vue'
import { useRouter } from 'vue-router'
import AppDialog from '@/components/common/AppDialog.vue'
import BasePagination from '@/components/common/BasePagination.vue'
import TableRowActions from '@/components/common/TableRowActions.vue'
import UserLink from '@/components/business/UserLink.vue'
import WithdrawAccountNumberCell from '@/components/business/WithdrawAccountNumberCell.vue'
import { deleteAppUserApi, getAppUserInfo, getAppUserPage, updateAppUserApi } from '@/api/modules/app-user'
import { loadChapterSetupOptions } from '@/api/modules/chapter-tree'
import { ensureGlobalCommissionDefaults } from '@/api/modules/promo-config'
import { updateUserWalletAmountApi } from '@/api/modules/user-wallet'
import { userWalletTransactionApi } from '@/api/modules/user-wallet-transaction'
import { withdrawalAccountApi } from '@/api/modules/withdrawal-account'
import {
  WALLET_DIRECTION_MAP,
  WALLET_TRANSACTION_STATUS_MAP,
  WALLET_TRANSACTION_TYPE_MAP,
  WITHDRAWAL_ACCOUNT_TYPE_MAP,
  formatEnumColumn,
} from '@/constants/admin-enums'
import {
  APP_USER_DETAIL_FIELDS,
  getAppUserStatusLabel,
  getAppUserStatusTagType,
  getAppUserSexLabel,
  parseAppUserSubjects,
  resolveAppUserDisplayName,
  formatAppUserWalletAmount,
  formatSuperiorSummary,
  getGlobalCommissionDefaults,
  commissionRateToPercent,
  commissionRateFromPercent,
} from '@/constants/app-user'
import FilterSelect from '@/components/common/FilterSelect.vue'
import FilterSummary from '@/components/common/FilterSummary.vue'
import { useFilterFormKey } from '@/composables/useFilterFormKey'
import { useSetupStageCascade } from '@/composables/useSetupStageCascade'
import { FILTER_SELECT_EMPTY, normalizeNumberFilterQuery } from '@/utils/filter-form'
import { EMPTY_SETUP_OPTIONS } from '@/utils/setup-options'
import { validateForm } from '@/utils/form'
import { confirmAction, showError, showSuccess } from '@/utils/modal'

const router = useRouter()

const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailTab = ref('basic')
const walletTxLoading = ref(false)
const walletTxList = ref([])
const walletTxTotal = ref(0)
const walletTxPageNum = ref(1)
const walletTxPageSize = ref(10)
const withdrawalLoading = ref(false)
const withdrawalList = ref([])
const memberLoading = ref(false)
const memberList = ref([])
const memberTotal = ref(0)
const memberPageNum = ref(1)
const memberPageSize = ref(10)
const formRef = ref()
const tableData = ref([])
const detailData = ref(null)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const editingId = ref(null)
const editTab = ref('basic')
const setupOptions = ref({ ...EMPTY_SETUP_OPTIONS })
const { filterFormKey, bumpFilterFormKey } = useFilterFormKey()

const USER_STATUS_OPTIONS = [
  { label: '正常', value: 1 },
  { label: '禁用', value: 2 },
]

const FILTER_FIELDS = [
  { key: 'username', label: '用户名', type: 'text', placeholder: '请输入用户名' },
  { key: 'phone', label: '手机号', type: 'text', placeholder: '请输入手机号' },
  { key: 'status', label: '状态', type: 'select', options: USER_STATUS_OPTIONS, placeholder: '全部状态' },
]

const filters = reactive({
  username: '',
  phone: '',
  status: FILTER_SELECT_EMPTY,
})

const form = reactive({
  userId: null,
  userName: '',
  nickname: '',
  phone: '',
  status: 1,
  grade: '',
  textbookVersion: '',
  subjects: [],
  profileReady: 1,
  isVip: 0,
  vipExpireAt: '',
  walletAmount: 0,
  commissionRateLevel1Percent: null,
  commissionRateLevel2Percent: null,
})

const {
  stageCode: formStageCode,
  gradeStages: formGradeStages,
  filteredGrades: formGrades,
  filteredSubjects: formSubjects,
  handleStageChange: handleFormStageChange,
  syncStageFromGrade: syncFormStageFromGrade,
} = useSetupStageCascade(setupOptions, {
  gradeRef: toRef(form, 'grade'),
  subjectsRef: toRef(form, 'subjects'),
  filterMode: false,
})

const walletAdjust = reactive({
  amount: 0,
  operator: 'incr',
})

const rules = {
  userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
}

const defaultCommissionPercents = computed(() => {
  const defaults = getGlobalCommissionDefaults()
  return {
    level1: commissionRateToPercent(defaults.level1),
    level2: commissionRateToPercent(defaults.level2),
  }
})

function formatDetailValue(field, row) {
  const raw = row?.[field.key]
  if (field.formatter) {
    return field.formatter(raw, row)
  }
  if (raw === null || raw === undefined || raw === '') {
    return '—'
  }
  return raw
}

function resetWalletAdjust() {
  Object.assign(walletAdjust, { amount: 0, operator: 'incr' })
}

async function loadSetupOptions() {
  try {
    setupOptions.value = await loadChapterSetupOptions()
  } catch {
    setupOptions.value = { ...EMPTY_SETUP_OPTIONS }
  }
}

function handleStudyGradeChange(gradeValue) {
  syncFormStageFromGrade(gradeValue)
}

async function loadData() {
  loading.value = true
  try {
    const data = await getAppUserPage({
      page: pageNum.value,
      limit: pageSize.value,
      username: filters.username || undefined,
      phone: filters.phone || undefined,
      status: normalizeNumberFilterQuery(filters.status),
    })
    tableData.value = data.list
    total.value = data.total
  } catch (error) {
    showError(error?.msg || error?.message || '加载用户列表失败')
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

function handleReset() {
  Object.assign(filters, { username: '', phone: '', status: FILTER_SELECT_EMPTY })
  bumpFilterFormKey()
  pageNum.value = 1
  loadData()
}

async function openDetail(row) {
  detailVisible.value = true
  detailLoading.value = true
  detailTab.value = 'basic'
  detailData.value = null
  walletTxList.value = []
  walletTxTotal.value = 0
  walletTxPageNum.value = 1
  withdrawalList.value = []
  memberList.value = []
  memberTotal.value = 0
  memberPageNum.value = 1
  try {
    await ensureGlobalCommissionDefaults()
    detailData.value = await getAppUserInfo(row.userId)
    await Promise.all([
      loadUserWalletTransactions(row.userId),
      loadUserWithdrawalAccounts(row.userId),
      loadDirectMembers(row.userId, detailData.value?.invitationCode),
    ])
  } catch (error) {
    showError(error?.msg || error?.message || '加载用户详情失败')
    detailVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

async function loadUserWalletTransactions(userId) {
  if (!userId) return
  walletTxLoading.value = true
  try {
    const data = await userWalletTransactionApi.page({
      page: walletTxPageNum.value,
      limit: walletTxPageSize.value,
      userId,
    })
    walletTxList.value = data.list
    walletTxTotal.value = data.total
  } catch {
    walletTxList.value = []
    walletTxTotal.value = 0
  } finally {
    walletTxLoading.value = false
  }
}

async function loadDirectMembers(userId, invitationCode) {
  if (!userId || !invitationCode) {
    memberList.value = []
    memberTotal.value = 0
    return
  }
  memberLoading.value = true
  try {
    const data = await getAppUserPage({
      page: memberPageNum.value,
      limit: memberPageSize.value,
      superiorInvitationCode: invitationCode,
    })
    memberList.value = data.list
    memberTotal.value = data.total
  } catch {
    memberList.value = []
    memberTotal.value = 0
  } finally {
    memberLoading.value = false
  }
}

function handleMemberPageChange(page) {
  memberPageNum.value = page
  if (detailData.value?.invitationCode) {
    loadDirectMembers(detailData.value.userId, detailData.value.invitationCode)
  }
}

function handleMemberSizeChange(size) {
  memberPageSize.value = size
  memberPageNum.value = 1
  if (detailData.value?.invitationCode) {
    loadDirectMembers(detailData.value.userId, detailData.value.invitationCode)
  }
}

async function loadUserWithdrawalAccounts(userId) {
  if (!userId) return
  withdrawalLoading.value = true
  try {
    const data = await withdrawalAccountApi.page({
      page: 1,
      limit: 50,
      userId,
      source: 1,
    })
    withdrawalList.value = data.list
  } catch {
    withdrawalList.value = []
  } finally {
    withdrawalLoading.value = false
  }
}

function handleWalletTxPageChange(page) {
  walletTxPageNum.value = page
  if (detailData.value?.userId) {
    loadUserWalletTransactions(detailData.value.userId)
  }
}

function handleWalletTxSizeChange(size) {
  walletTxPageSize.value = size
  walletTxPageNum.value = 1
  if (detailData.value?.userId) {
    loadUserWalletTransactions(detailData.value.userId)
  }
}

async function openEdit(row) {
  editingId.value = row.userId
  editTab.value = 'basic'
  dialogVisible.value = true
  dialogLoading.value = true
  resetWalletAdjust()
  try {
    const data = await getAppUserInfo(row.userId)
    Object.assign(form, {
      userId: data.userId,
      userName: data.userName || '',
      nickname: data.nickname || '',
      phone: data.phone || '',
      status: data.status ?? 1,
      grade: data.grade || '',
      textbookVersion: data.textbookVersion || '',
      subjects: parseAppUserSubjects(data.subjects),
      profileReady: Number(data.profileReady) === 1 ? 1 : 0,
      isVip: Number(data.isVip) === 1 ? 1 : 0,
      vipExpireAt: data.vipExpireAt || '',
      walletAmount: Number(data.walletAmount || 0),
      commissionRateLevel1Percent: commissionRateToPercent(data.commissionRateLevel1),
      commissionRateLevel2Percent: commissionRateToPercent(data.commissionRateLevel2),
    })
    syncFormStageFromGrade(form.grade)
  } catch (error) {
    showError(error?.msg || error?.message || '加载用户失败')
    dialogVisible.value = false
  } finally {
    dialogLoading.value = false
  }
}

async function handleSubmit() {
  const valid = await validateForm(formRef)
  if (!valid) return
  dialogLoading.value = true
  try {
    await updateAppUserApi({
      userId: form.userId,
      userName: form.userName,
      nickname: form.nickname,
      phone: form.phone,
      status: form.status,
      grade: form.grade,
      textbookVersion: form.textbookVersion,
      subjects: form.subjects,
      profileReady: form.profileReady,
      isVip: form.isVip,
      vipExpireAt: form.isVip === 1 ? (form.vipExpireAt || null) : null,
      commissionRateLevel1: commissionRateFromPercent(form.commissionRateLevel1Percent),
      commissionRateLevel2: commissionRateFromPercent(form.commissionRateLevel2Percent),
    })
    if (walletAdjust.amount > 0) {
      await updateUserWalletAmountApi({
        userId: form.userId,
        operator: walletAdjust.operator,
        amount: walletAdjust.amount,
      })
    }
    showSuccess('用户已更新')
    dialogVisible.value = false
    loadData()
  } finally {
    dialogLoading.value = false
  }
}

async function handleDelete(row) {
  await confirmAction(`确认删除用户“${row.userName || row.phone}”吗？`)
  await deleteAppUserApi({ userId: row.userId })
  showSuccess('用户已删除')
  loadData()
}

function openUserNotebooks(row) {
  router.push({
    path: '/business/user-notebooks/detail',
    query: { userId: row.userId },
  })
}

onMounted(async () => {
  await Promise.all([ensureGlobalCommissionDefaults(), loadSetupOptions(), loadData()])
})
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <strong>C端用户</strong>
      </div>
    </template>
    <el-form :key="filterFormKey" :inline="true" class="search-grid">
      <el-form-item label="用户名">
        <el-input v-model="filters.username" clearable placeholder="请输入用户名" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="filters.phone" clearable placeholder="请输入手机号" />
      </el-form-item>
      <el-form-item label="状态">
        <FilterSelect v-model="filters.status" clearable placeholder="全部状态">
          <el-option v-for="item in USER_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </FilterSelect>
      </el-form-item>
      <el-form-item>
        <el-space>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-space>
      </el-form-item>
    </el-form>
    <FilterSummary :filters="filters" :fields="FILTER_FIELDS" />
    <el-table v-loading="loading" :data="tableData" border empty-text="暂无用户">
      <el-table-column prop="userId" label="用户ID" width="80" />
      <el-table-column label="用户" min-width="140">
        <template #default="{ row }">
          <UserLink :user-id="row.userId" :label="resolveAppUserDisplayName(row)" />
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" min-width="140">
        <template #default="{ row }">
          <UserLink :user-id="row.userId" :phone="row.phone" />
        </template>
      </el-table-column>
      <el-table-column label="上级" min-width="130" show-overflow-tooltip>
        <template #default="{ row }">
          <UserLink
            v-if="row.superiorUserId"
            :user-id="row.superiorUserId"
            :label="formatSuperiorSummary(row)"
          />
          <span v-else>{{ formatSuperiorSummary(row) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="invitationCode" label="邀请码" min-width="110" show-overflow-tooltip>
        <template #default="{ row }">{{ row.invitationCode || '—' }}</template>
      </el-table-column>
      <el-table-column label="下级人数" width="90" align="center">
        <template #default="{ row }">{{ row.directMemberCount ?? 0 }}</template>
      </el-table-column>
      <el-table-column label="钱包余额" min-width="100">
        <template #default="{ row }">{{ formatAppUserWalletAmount(row.walletAmount) }}</template>
      </el-table-column>
      <el-table-column label="提现账户" min-width="140" show-overflow-tooltip>
        <template #default="{ row }">{{ row.withdrawalAccountSummary || '—' }}</template>
      </el-table-column>
      <el-table-column label="年级/学科" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">
          {{ [row.gradeShort || row.grade, parseAppUserSubjects(row.subjects).join('、')].filter(Boolean).join(' · ') || '—' }}
        </template>
      </el-table-column>
      <el-table-column prop="isVip" label="会员" min-width="90">
        <template #default="{ row }">
          <el-tag :type="row.isVip === 1 ? 'warning' : 'info'">{{ row.isVip === 1 ? '会员' : '普通' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="getAppUserStatusTagType(row.status)">{{ getAppUserStatusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="180" />
      <el-table-column label="操作" min-width="280" fixed="right">
        <template #default="{ row }">
          <TableRowActions>
            <el-button link type="primary" @click="openDetail(row)">查看详情</el-button>
            <el-button link type="primary" @click="openUserNotebooks(row)">查看错题</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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
    <AppDialog v-model="dialogVisible" title="编辑用户" :loading="dialogLoading" width="760px" @confirm="handleSubmit">
      <el-tabs v-model="editTab">
        <el-tab-pane label="账号信息" name="basic">
          <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
            <el-form-item label="用户名" prop="userName">
              <el-input v-model="form.userName" />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="form.nickname" placeholder="用户端展示名" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="form.status">
                <el-option label="正常" :value="1" />
                <el-option label="禁用" :value="2" />
              </el-select>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="学习计划" name="study">
          <el-form :model="form" label-width="96px">
            <el-form-item label="学段">
              <el-select
                :model-value="formStageCode"
                clearable
                filterable
                placeholder="请选择学段"
                @update:model-value="handleFormStageChange"
              >
                <el-option
                  v-for="item in formGradeStages"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="年级">
              <el-select
                v-model="form.grade"
                clearable
                filterable
                placeholder="请选择年级"
                @change="handleStudyGradeChange"
              >
                <el-option
                  v-for="item in formGrades"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="教材版本">
              <el-select v-model="form.textbookVersion" clearable filterable placeholder="请选择教材版本">
                <el-option
                  v-for="item in setupOptions.versions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.label"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="学科">
              <el-select v-model="form.subjects" multiple clearable filterable placeholder="请选择学科">
                <el-option
                  v-for="item in formSubjects"
                  :key="item.value"
                  :label="item.label"
                  :value="item.label"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="资料完善">
              <el-switch v-model="form.profileReady" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="会员" name="vip">
          <el-form :model="form" label-width="108px">
            <el-form-item label="会员状态">
              <el-radio-group v-model="form.isVip">
                <el-radio :value="0">普通用户</el-radio>
                <el-radio :value="1">VIP 会员</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item v-if="form.isVip === 1" label="到期时间">
              <el-date-picker
                v-model="form.vipExpireAt"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="选择会员到期时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="推广佣金" name="commission">
          <el-form :model="form" label-width="120px">
            <el-alert
              type="info"
              :closable="false"
              show-icon
              title="留空表示使用「推广佣金规则」页的全局默认比例；填写后仅对该用户生效。"
              style="margin-bottom: 16px"
            />
            <el-form-item label="一级佣金">
              <el-input-number
                v-model="form.commissionRateLevel1Percent"
                :min="0"
                :max="100"
                :step="0.1"
                :precision="1"
                controls-position="right"
                :placeholder="defaultCommissionPercents.level1 != null ? String(defaultCommissionPercents.level1) : '10'"
                style="width: 220px"
              />
              <span class="field-suffix">%（留空跟随全局默认）</span>
            </el-form-item>
            <el-form-item label="二级佣金">
              <el-input-number
                v-model="form.commissionRateLevel2Percent"
                :min="0"
                :max="100"
                :step="0.1"
                :precision="1"
                controls-position="right"
                :placeholder="defaultCommissionPercents.level2 != null ? String(defaultCommissionPercents.level2) : '5'"
                style="width: 220px"
              />
              <span class="field-suffix">%（留空跟随全局默认）</span>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="钱包" name="wallet">
          <el-form :model="walletAdjust" label-width="108px">
            <el-form-item label="当前余额">
              <el-input :model-value="`¥${form.walletAmount}`" disabled />
            </el-form-item>
            <el-form-item label="调整金额">
              <el-input-number v-model="walletAdjust.amount" :min="0" :precision="2" :step="1" />
            </el-form-item>
            <el-form-item label="调整方向">
              <el-radio-group v-model="walletAdjust.operator">
                <el-radio value="incr">增加</el-radio>
                <el-radio value="decr">扣减</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-alert
              type="info"
              :closable="false"
              show-icon
              title="金额为 0 时不调整钱包；保存时会写入钱包流水。"
            />
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </AppDialog>
    <AppDialog v-model="detailVisible" title="用户详情" :show-footer="false" :loading="detailLoading" width="960px">
      <template v-if="detailData">
        <el-tabs v-model="detailTab">
          <el-tab-pane label="基本信息" name="basic">
            <el-descriptions :column="2" border>
              <el-descriptions-item
                v-for="field in APP_USER_DETAIL_FIELDS"
                :key="field.key"
                :label="field.label"
                :span="['subjects', 'maOpenId', 'mpOpenId'].includes(field.key) ? 2 : 1"
              >
                <UserLink
                  v-if="field.key === 'userId'"
                  :user-id="detailData.userId"
                  :label="String(detailData.userId)"
                />
                <UserLink
                  v-else-if="field.key === 'phone'"
                  :user-id="detailData.userId"
                  :phone="detailData.phone"
                />
                <UserLink
                  v-else-if="field.key === 'userName' || field.key === 'nickname'"
                  :user-id="detailData.userId"
                  :label="formatDetailValue(field, detailData)"
                />
                <template v-else>
                  {{ formatDetailValue(field, detailData) }}
                </template>
              </el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          <el-tab-pane label="钱包流水" name="walletTx">
            <div v-if="detailData.walletAmount != null" class="detail-wallet-summary">
              当前余额：<strong>{{ formatAppUserWalletAmount(detailData.walletAmount) }}</strong>
            </div>
            <el-table v-loading="walletTxLoading" :data="walletTxList" border size="small" empty-text="暂无流水">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="title" label="标题" min-width="140" show-overflow-tooltip />
              <el-table-column prop="amount" label="金额" width="100" />
              <el-table-column label="交易类型" width="110">
                <template #default="{ row }">{{ formatEnumColumn(WALLET_TRANSACTION_TYPE_MAP)(row.transactionType) }}</template>
              </el-table-column>
              <el-table-column label="方向" width="90">
                <template #default="{ row }">{{ formatEnumColumn(WALLET_DIRECTION_MAP)(row.direction) }}</template>
              </el-table-column>
              <el-table-column label="状态" width="100">
                <template #default="{ row }">{{ formatEnumColumn(WALLET_TRANSACTION_STATUS_MAP)(row.status) }}</template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" min-width="170" />
            </el-table>
            <BasePagination
              :current-page="walletTxPageNum"
              :page-size="walletTxPageSize"
              :total="walletTxTotal"
              @update:current-page="handleWalletTxPageChange"
              @update:page-size="handleWalletTxSizeChange"
            />
          </el-tab-pane>
          <el-tab-pane label="推广下级" name="members">
            <el-alert
              v-if="!detailData.invitationCode"
              type="info"
              :closable="false"
              show-icon
              title="该用户尚未生成邀请码，暂无直接下级"
              style="margin-bottom: 12px"
            />
            <el-table v-else v-loading="memberLoading" :data="memberList" border size="small" empty-text="暂无直接下级">
              <el-table-column prop="userId" label="用户ID" width="80" />
              <el-table-column label="用户" min-width="120">
                <template #default="{ row }">
                  <UserLink :user-id="row.userId" :label="resolveAppUserDisplayName(row)" />
                </template>
              </el-table-column>
              <el-table-column prop="phone" label="手机号" min-width="120" />
              <el-table-column label="会员" width="90">
                <template #default="{ row }">
                  <el-tag :type="row.isVip === 1 ? 'warning' : 'info'" size="small">
                    {{ row.isVip === 1 ? '会员' : '普通' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="注册时间" min-width="170" />
            </el-table>
            <BasePagination
              v-if="detailData.invitationCode"
              :current-page="memberPageNum"
              :page-size="memberPageSize"
              :total="memberTotal"
              @update:current-page="handleMemberPageChange"
              @update:page-size="handleMemberSizeChange"
            />
          </el-tab-pane>
          <el-tab-pane label="提现账户" name="withdrawal">
            <el-table v-loading="withdrawalLoading" :data="withdrawalList" border size="small" empty-text="暂无提现账户">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="accountName" label="账户名称" min-width="120" />
              <el-table-column label="账号/收款码" min-width="140">
                <template #default="{ row }">
                  <WithdrawAccountNumberCell :row="row" />
                </template>
              </el-table-column>
              <el-table-column label="类型" width="100">
                <template #default="{ row }">{{ formatEnumColumn(WITHDRAWAL_ACCOUNT_TYPE_MAP)(row.type) }}</template>
              </el-table-column>
              <el-table-column prop="realName" label="真实姓名" min-width="100" />
              <el-table-column prop="createTime" label="创建时间" min-width="170" />
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </template>
    </AppDialog>
  </el-card>
</template>

<style scoped>
.field-suffix {
  margin-left: 8px;
  color: var(--el-text-color-secondary);
}
.detail-wallet-summary {
  margin-bottom: 12px;
  font-size: 14px;
  color: var(--el-text-color-regular);
}
</style>
