<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import FilterSelect from '@/components/common/FilterSelect.vue'
import FilterSummary from '@/components/common/FilterSummary.vue'
import { useFilterFormKey } from '@/composables/useFilterFormKey'
import { getReferralMembersPage } from '@/api/modules/referral-members'
import { REFERRAL_MEMBER_PAY_OPTIONS, maskPhone, resolveMemberPayStatus } from '@/constants/promo'
import { FILTER_SELECT_EMPTY, normalizeNumberFilterQuery } from '@/utils/filter-form'
import { showError } from '@/utils/modal'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const inviterInfo = ref(null)
const { filterFormKey, bumpFilterFormKey } = useFilterFormKey()

const FILTER_FIELDS = [
  { key: 'inviterPhone', label: '邀请人手机号', type: 'text', placeholder: '精确匹配' },
  { key: 'inviterUserName', label: '邀请人用户名', type: 'text', placeholder: '精确匹配' },
  { key: 'memberPhone', label: '下级手机号', type: 'text', placeholder: '精确匹配' },
  { key: 'memberUserName', label: '下级用户名', type: 'text', placeholder: '精确匹配' },
  { key: 'payStatus', label: '付费状态', type: 'select', options: REFERRAL_MEMBER_PAY_OPTIONS, placeholder: '全部状态' },
]

const filters = reactive({
  inviterPhone: '',
  inviterUserName: '',
  memberPhone: '',
  memberUserName: '',
  payStatus: FILTER_SELECT_EMPTY,
})

const hasInviterQuery = computed(() => Boolean(filters.inviterPhone || filters.inviterUserName))

function buildQueryParams() {
  return {
    page: pageNum.value,
    limit: pageSize.value,
    inviterPhone: filters.inviterPhone || undefined,
    inviterUserName: filters.inviterUserName || undefined,
    memberPhone: filters.memberPhone || undefined,
    memberUserName: filters.memberUserName || undefined,
    payStatus: normalizeNumberFilterQuery(filters.payStatus),
  }
}

function resolveMemberName(row) {
  return row?.nickname || row?.userName || '-'
}

async function loadData() {
  if (!hasInviterQuery.value) {
    inviterInfo.value = null
    tableData.value = []
    total.value = 0
    return
  }

  loading.value = true
  try {
    const data = await getReferralMembersPage(buildQueryParams())
    inviterInfo.value = data.inviter
    tableData.value = data.list
    total.value = data.total
  } catch (error) {
    showError(error?.msg || error?.message || '加载下级关系失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

function handleReset() {
  Object.assign(filters, {
    inviterPhone: '',
    inviterUserName: '',
    memberPhone: '',
    memberUserName: '',
    payStatus: FILTER_SELECT_EMPTY,
  })
  bumpFilterFormKey()
  pageNum.value = 1
  inviterInfo.value = null
  tableData.value = []
  total.value = 0
}
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <strong>推广下级关系</strong>
      </div>
    </template>

    <p class="page-desc">
      按邀请人手机号或用户名查询其直接下级（superior_invitation_code 匹配邀请码）。
      展示下级昵称、注册时间与会员付费状态。
    </p>

    <el-form :key="filterFormKey" :inline="true" class="filter-form" @submit.prevent="handleSearch">
      <el-form-item label="邀请人手机号">
        <el-input v-model="filters.inviterPhone" clearable placeholder="精确匹配" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="邀请人用户名">
        <el-input v-model="filters.inviterUserName" clearable placeholder="精确匹配" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="下级手机号">
        <el-input v-model="filters.memberPhone" clearable placeholder="精确匹配" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="下级用户名">
        <el-input v-model="filters.memberUserName" clearable placeholder="精确匹配" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="付费状态">
        <FilterSelect v-model="filters.payStatus" clearable placeholder="全部状态">
          <el-option
            v-for="item in REFERRAL_MEMBER_PAY_OPTIONS"
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

    <el-alert
      v-if="hasInviterQuery && inviterInfo && !inviterInfo.invitationCode"
      type="warning"
      :closable="false"
      title="该邀请人尚未生成邀请码，暂无下级数据"
      class="inviter-alert"
    />
    <div v-else-if="inviterInfo" class="inviter-summary">
      当前邀请人：<strong>{{ inviterInfo.nickname || inviterInfo.userName }}</strong>
      · 手机号 {{ maskPhone(inviterInfo.phone) }}
      · 邀请码 {{ inviterInfo.invitationCode }}
    </div>

    <el-table v-loading="loading" :data="tableData" border empty-text="请先输入邀请人手机号或用户名后查询">
      <el-table-column label="下级昵称/用户名" min-width="140">
        <template #default="{ row }">{{ resolveMemberName(row) }}</template>
      </el-table-column>
      <el-table-column label="手机号" width="130">
        <template #default="{ row }">{{ maskPhone(row.phone) }}</template>
      </el-table-column>
      <el-table-column label="注册时间" min-width="170">
        <template #default="{ row }">{{ row.createTime || '-' }}</template>
      </el-table-column>
      <el-table-column label="付费状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.isVip === 1 ? 'success' : 'info'">
            {{ resolveMemberPayStatus(row) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="上级邀请码" min-width="140">
        <template #default="{ row }">{{ row.superiorInvitationCode || '-' }}</template>
      </el-table-column>
    </el-table>

    <BasePagination
      v-if="hasInviterQuery"
      :current-page="pageNum"
      :page-size="pageSize"
      :total="total"
      @update:current-page="pageNum = $event; loadData()"
      @update:page-size="pageSize = $event; pageNum = 1; loadData()"
    />
  </el-card>
</template>

<style scoped lang="scss">
.page-desc {
  margin: 0 0 16px;
  color: var(--el-text-color-secondary);
  line-height: 1.6;
}

.inviter-alert {
  margin-bottom: 12px;
}

.inviter-summary {
  margin-bottom: 12px;
  padding: 10px 12px;
  background: var(--el-fill-color-light);
  border-radius: 6px;
  color: var(--el-text-color-regular);
}
</style>
