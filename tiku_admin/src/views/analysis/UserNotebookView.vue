<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BasePagination from '@/components/common/BasePagination.vue'
import UserLink from '@/components/business/UserLink.vue'
import TableRowActions from '@/components/common/TableRowActions.vue'
import { getUserNotebookUserPageApi } from '@/api/modules/user-notebook'
import { resolveAppUserDisplayName } from '@/constants/app-user'
import FilterSelect from '@/components/common/FilterSelect.vue'
import FilterSummary from '@/components/common/FilterSummary.vue'
import { FILTER_SELECT_EMPTY, isFilterEmpty } from '@/utils/filter-form'
import { showError } from '@/utils/modal'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const MASTERED_OPTIONS = [
  { label: '待复习', value: '0' },
  { label: '已掌握', value: '1' },
]

const FILTER_FIELDS = [
  { key: 'phone', label: '手机号', type: 'text', placeholder: '模糊搜索' },
  { key: 'userName', label: '用户名', type: 'text', placeholder: '模糊搜索' },
  { key: 'subject', label: '学科', type: 'text', placeholder: '如：数学' },
  { key: 'mastered', label: '掌握状态', type: 'select', options: MASTERED_OPTIONS, placeholder: '全部状态' },
]

const filters = reactive({
  phone: '',
  userName: '',
  subject: '',
  mastered: FILTER_SELECT_EMPTY,
})

function buildQueryParams() {
  return {
    page: pageNum.value,
    limit: pageSize.value,
    phone: filters.phone || undefined,
    userName: filters.userName || undefined,
    subject: filters.subject || undefined,
    mastered: isFilterEmpty(filters.mastered) ? undefined : filters.mastered === '1',
  }
}

async function loadData() {
  loading.value = true
  try {
    const data = await getUserNotebookUserPageApi(buildQueryParams())
    tableData.value = data.list
    total.value = data.total
  } catch (error) {
    showError(error?.msg || error?.message || '加载用户错题统计失败')
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
    phone: '',
    userName: '',
    subject: '',
    mastered: FILTER_SELECT_EMPTY,
  })
  pageNum.value = 1
  router.replace({ path: route.path })
  loadData()
}

function openUserNotebookDetail(row) {
  router.push({
    path: '/business/user-notebooks/detail',
    query: { userId: row.userId },
  })
}

onMounted(() => {
  if (route.query.userId) {
    openUserNotebookDetail({ userId: route.query.userId })
    return
  }
  loadData()
})
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <strong>用户错题本</strong>
      </div>
    </template>

    <p class="page-desc">
      统计有错题的用户，每位用户仅展示一行。点击「错题列表」查看该用户的全部错题，并支持按需导出。
    </p>

    <el-form :inline="true" class="filter-form" @submit.prevent="handleSearch">
      <el-form-item label="手机号">
        <el-input v-model="filters.phone" clearable placeholder="模糊搜索" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="用户名">
        <el-input v-model="filters.userName" clearable placeholder="模糊搜索" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="学科">
        <el-input v-model="filters.subject" clearable placeholder="如：数学" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="掌握状态">
        <FilterSelect v-model="filters.mastered" clearable placeholder="全部状态">
          <el-option label="待复习" value="0" />
          <el-option label="已掌握" value="1" />
        </FilterSelect>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <FilterSummary :filters="filters" :fields="FILTER_FIELDS" />

    <el-table v-loading="loading" :data="tableData" border empty-text="暂无有错题的用户">
      <el-table-column label="用户" min-width="130" fixed="left">
        <template #default="{ row }">
          <UserLink
            v-if="row.userId"
            :user-id="row.userId"
            :label="resolveAppUserDisplayName(row)"
          />
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" min-width="120" />
      <el-table-column prop="gradeShort" label="年级" width="80" />
      <el-table-column prop="subjects" label="涉及学科" min-width="140" show-overflow-tooltip />
      <el-table-column prop="wrongTotal" label="错题总数" width="90" />
      <el-table-column prop="pendingCount" label="待复习" width="80">
        <template #default="{ row }">
          <el-tag type="warning">{{ row.pendingCount ?? 0 }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="masteredCount" label="已掌握" width="80">
        <template #default="{ row }">
          <el-tag type="success">{{ row.masteredCount ?? 0 }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastPracticeAt" label="最近练习" width="170" />
      <el-table-column label="操作" min-width="120" fixed="right">
        <template #default="{ row }">
          <TableRowActions>
            <el-button link type="primary" @click="openUserNotebookDetail(row)">错题列表</el-button>
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
</template>

<style scoped>
.page-desc {
  margin: 0 0 16px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
  line-height: 1.6;
}
.filter-form { margin-bottom: 16px; }
</style>
