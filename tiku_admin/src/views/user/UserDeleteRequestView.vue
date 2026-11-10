<script setup>
import { onMounted, reactive, ref } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import TableRowActions from '@/components/common/TableRowActions.vue'
import UserLink from '@/components/business/UserLink.vue'
import {
  cancelUserDeleteRequestApi,
  executeUserDeleteRequestApi,
  getUserDeleteRequestPageApi,
} from '@/api/modules/user-delete-request'
import {
  USER_DELETE_REQUEST_STATUS_OPTIONS,
  getUserDeleteRequestStatusLabel,
  getUserDeleteRequestStatusTagType,
} from '@/constants/user-delete-request'
import FilterSelect from '@/components/common/FilterSelect.vue'
import FilterSummary from '@/components/common/FilterSummary.vue'
import { useFilterFormKey } from '@/composables/useFilterFormKey'
import { FILTER_SELECT_EMPTY, isFilterEmpty } from '@/utils/filter-form'
import { confirmAction, showError, showSuccess } from '@/utils/modal'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const { filterFormKey, bumpFilterFormKey } = useFilterFormKey()

const FILTER_FIELDS = [
  { key: 'phone', label: '手机号', type: 'text', placeholder: '请输入手机号' },
  { key: 'userName', label: '用户名', type: 'text', placeholder: '昵称/用户名' },
  { key: 'status', label: '状态', type: 'select', options: USER_DELETE_REQUEST_STATUS_OPTIONS, placeholder: '全部状态' },
]

const filters = reactive({
  phone: '',
  userName: '',
  status: 'pending',
})

async function loadData() {
  loading.value = true
  try {
    const data = await getUserDeleteRequestPageApi({
      page: pageNum.value,
      limit: pageSize.value,
      phone: filters.phone || undefined,
      userName: filters.userName || undefined,
      status: isFilterEmpty(filters.status) ? undefined : filters.status,
    })
    tableData.value = data.list || []
    total.value = data.total || 0
  } catch (error) {
    showError(error?.msg || error?.message || '加载注销申请失败')
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
  filters.phone = ''
  filters.userName = ''
  filters.status = 'pending'
  bumpFilterFormKey()
  pageNum.value = 1
  loadData()
}

function handlePageChange(page) {
  pageNum.value = page
  loadData()
}

function handleSizeChange(size) {
  pageSize.value = size
  pageNum.value = 1
  loadData()
}

async function handleCancel(row) {
  await confirmAction(`确认撤销用户 ${row.userId} 的注销申请吗？撤销后账号与数据保持不变。`)
  await cancelUserDeleteRequestApi({ id: row.id })
  showSuccess('已撤销注销申请')
  loadData()
}

async function handleExecute(row) {
  await confirmAction(`确认立即注销用户 ${row.userId} 吗？该操作不可恢复。`)
  await executeUserDeleteRequestApi({ id: row.id })
  showSuccess('账号已执行注销')
  loadData()
}

onMounted(loadData)
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <strong>账号注销申请</strong>
        <span class="card-sub">用户提交后进入 7 天冷静期，期间数据保留，可撤销或立即执行</span>
      </div>
    </template>

    <el-form :key="filterFormKey" :inline="true" class="search-grid">
      <el-form-item label="手机号">
        <el-input v-model="filters.phone" clearable placeholder="请输入手机号" />
      </el-form-item>
      <el-form-item label="用户名">
        <el-input v-model="filters.userName" clearable placeholder="昵称/用户名" />
      </el-form-item>
      <el-form-item label="状态">
        <FilterSelect v-model="filters.status" clearable placeholder="全部状态">
          <el-option
            v-for="item in USER_DELETE_REQUEST_STATUS_OPTIONS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
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

    <el-table v-loading="loading" :data="tableData" border empty-text="暂无注销申请">
      <el-table-column label="用户" min-width="140">
        <template #default="{ row }">
          <UserLink :user-id="row.userId" :label="row.nickname || row.userName || `用户${row.userId}`" />
        </template>
      </el-table-column>
      <el-table-column label="手机号" min-width="140">
        <template #default="{ row }">
          <UserLink :user-id="row.userId" :phone="row.phone" />
        </template>
      </el-table-column>
      <el-table-column label="状态" min-width="120">
        <template #default="{ row }">
          <el-tag :type="getUserDeleteRequestStatusTagType(row.status)">
            {{ getUserDeleteRequestStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="requestTime" label="申请时间" min-width="170" />
      <el-table-column prop="effectiveTime" label="预计执行时间" min-width="170" />
      <el-table-column label="剩余天数" min-width="100">
        <template #default="{ row }">
          {{ row.status === 'pending' ? (row.remainingDays ?? '—') : '—' }}
        </template>
      </el-table-column>
      <el-table-column prop="completeTime" label="完成时间" min-width="170" />
      <el-table-column prop="adminRemark" label="备注" min-width="160" show-overflow-tooltip />
      <el-table-column label="操作" min-width="220" fixed="right">
        <template #default="{ row }">
          <TableRowActions v-if="row.status === 'pending'">
            <el-button link type="primary" @click="handleCancel(row)">撤销申请</el-button>
            <el-button link type="danger" @click="handleExecute(row)">立即注销</el-button>
          </TableRowActions>
          <span v-else>—</span>
        </template>
      </el-table-column>
    </el-table>

    <BasePagination
      :total="total"
      :page="pageNum"
      :page-size="pageSize"
      @page-change="handlePageChange"
      @size-change="handleSizeChange"
    />
  </el-card>
</template>

<style scoped>
.card-header {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.card-sub {
  font-size: 13px;
  color: #667085;
  font-weight: 400;
}
</style>
