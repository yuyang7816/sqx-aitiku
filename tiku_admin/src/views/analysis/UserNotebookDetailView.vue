<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BasePagination from '@/components/common/BasePagination.vue'
import UserLink from '@/components/business/UserLink.vue'
import { getUserNotebookPageApi } from '@/api/modules/user-notebook'
import { loadPaperSetupOptions } from '@/api/modules/paper-set'
import { resolveAppUserDisplayName } from '@/constants/app-user'
import FilterSelect from '@/components/common/FilterSelect.vue'
import FilterSummary from '@/components/common/FilterSummary.vue'
import { FILTER_SELECT_EMPTY, isFilterEmpty } from '@/utils/filter-form'
import { buildAdminNotebookExportPayload, printAdminNotebookExport } from '@/utils/notebook-export'
import { showError, showSuccess } from '@/utils/modal'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const userInfo = ref(null)
const setupOptions = ref({ subjects: [] })
const selectedIds = ref([])
const exportVisible = ref(false)
const exportLoading = ref(false)

const exportForm = reactive({
  subjects: [],
  includeAnswerAnalysis: true,
})

const MASTERED_OPTIONS = [
  { label: '待复习', value: '0' },
  { label: '已掌握', value: '1' },
]

const FILTER_FIELDS = [
  { key: 'subject', label: '学科', type: 'text', placeholder: '如：数学' },
  { key: 'mastered', label: '掌握状态', type: 'select', options: MASTERED_OPTIONS, placeholder: '全部状态' },
]

const filters = reactive({
  subject: '',
  mastered: FILTER_SELECT_EMPTY,
})

const userId = computed(() => {
  const value = Number(route.query.userId)
  return Number.isFinite(value) && value > 0 ? value : null
})

const userLabel = computed(() => resolveAppUserDisplayName(userInfo.value || {}))

const availableSubjects = computed(() => {
  const fromDict = (setupOptions.value.subjects || []).map((item) => item.value || item.label)
  const fromRows = tableData.value.map((row) => row.subject).filter(Boolean)
  return [...new Set([...fromDict, ...fromRows])]
})

const exportCandidateItems = computed(() => {
  const selectedSet = new Set(selectedIds.value)
  if (!selectedSet.size) {
    return tableData.value
  }
  return tableData.value.filter((row) => selectedSet.has(row.notebookId))
})

function buildQueryParams() {
  return {
    page: pageNum.value,
    limit: pageSize.value,
    userId: userId.value || undefined,
    subject: filters.subject || undefined,
    mastered: isFilterEmpty(filters.mastered) ? undefined : filters.mastered === '1',
  }
}

async function loadSetupOptions() {
  try {
    setupOptions.value = await loadPaperSetupOptions()
  } catch (error) {
    showError(error?.msg || error?.message || '加载学科选项失败')
  }
}

async function loadData() {
  if (!userId.value) {
    showError('缺少用户 ID')
    return
  }
  loading.value = true
  try {
    const data = await getUserNotebookPageApi(buildQueryParams())
    tableData.value = data.list
    total.value = data.total
    if (!userInfo.value && data.list?.length) {
      userInfo.value = {
        userId: data.list[0].userId,
        userName: data.list[0].userName,
        nickname: data.list[0].nickname,
        phone: data.list[0].phone,
        gradeShort: data.list[0].gradeShort,
      }
    }
    selectedIds.value = selectedIds.value.filter((id) => data.list.some((row) => row.notebookId === id))
  } catch (error) {
    showError(error?.msg || error?.message || '加载错题列表失败')
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
    subject: '',
    mastered: FILTER_SELECT_EMPTY,
  })
  pageNum.value = 1
  loadData()
}

function goBack() {
  router.push('/business/user-notebooks')
}

function toggleRowSelection(row, checked) {
  const id = row.notebookId
  if (!id) {
    return
  }
  if (checked) {
    if (!selectedIds.value.includes(id)) {
      selectedIds.value.push(id)
    }
    return
  }
  selectedIds.value = selectedIds.value.filter((item) => item !== id)
}

function toggleSelectAll(checked) {
  if (!checked) {
    selectedIds.value = []
    return
  }
  selectedIds.value = tableData.value.map((row) => row.notebookId).filter(Boolean)
}

const allSelected = computed(() => (
  tableData.value.length > 0
  && tableData.value.every((row) => selectedIds.value.includes(row.notebookId))
))

function openExportDialog() {
  if (!tableData.value.length) {
    showError('当前暂无错题可导出')
    return
  }
  exportForm.subjects = []
  exportForm.includeAnswerAnalysis = true
  exportVisible.value = true
}

async function handleExport() {
  exportLoading.value = true
  try {
    const payload = buildAdminNotebookExportPayload({
      items: exportCandidateItems.value,
      subjects: exportForm.subjects,
      includeAnswerAnalysis: exportForm.includeAnswerAnalysis,
      userLabel: userLabel.value,
    })
    if (!payload.items.length) {
      showError('没有符合导出条件的题目，请调整学科或勾选题目')
      return
    }
    printAdminNotebookExport(payload)
    exportVisible.value = false
    showSuccess(`已生成 ${payload.count} 题的导出预览`)
  } catch (error) {
    showError(error?.message || '导出失败')
  } finally {
    exportLoading.value = false
  }
}

watch(
  () => route.query.userId,
  () => {
    userInfo.value = null
    selectedIds.value = []
    pageNum.value = 1
    loadData()
  }
)

onMounted(async () => {
  if (!userId.value) {
    showError('缺少用户 ID')
    goBack()
    return
  }
  await loadSetupOptions()
  await loadData()
})
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header card-header--split">
        <div class="header-left">
          <el-button link type="primary" @click="goBack">← 返回用户列表</el-button>
          <strong class="header-title">错题列表</strong>
          <span v-if="userInfo" class="header-user">
            <UserLink :user-id="userInfo.userId" :label="userLabel" />
            <span v-if="userInfo.phone"> · {{ userInfo.phone }}</span>
            <span v-if="userInfo.gradeShort"> · {{ userInfo.gradeShort }}</span>
          </span>
        </div>
        <el-button type="primary" @click="openExportDialog">导出</el-button>
      </div>
    </template>

    <p class="page-desc">
      勾选需要导出的题目（不勾选则导出当前页全部）。导出时可选择学科，以及是否包含答案与解析：不含答案适合模拟考试，含答案适合错题分析。
    </p>

    <el-form :inline="true" class="filter-form" @submit.prevent="handleSearch">
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

    <div v-if="selectedIds.length" class="selection-tip">
      已选 {{ selectedIds.length }} 题用于导出
    </div>

    <el-table v-loading="loading" :data="tableData" border empty-text="该用户暂无错题">
      <el-table-column width="48" fixed="left">
        <template #header>
          <el-checkbox
            :model-value="allSelected"
            :indeterminate="selectedIds.length > 0 && !allSelected"
            @change="toggleSelectAll"
          />
        </template>
        <template #default="{ row }">
          <el-checkbox
            :model-value="selectedIds.includes(row.notebookId)"
            @change="(checked) => toggleRowSelection(row, checked)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="subject" label="学科" width="100" />
      <el-table-column label="掌握状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.mastered ? 'success' : 'warning'">{{ row.mastered ? '已掌握' : '待复习' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="wrongCount" label="错次" width="70" />
      <el-table-column prop="title" label="题干" min-width="220" show-overflow-tooltip />
      <el-table-column prop="wrong" label="错选答案" min-width="100" show-overflow-tooltip />
      <el-table-column prop="right" label="正确答案" min-width="100" show-overflow-tooltip />
      <el-table-column prop="lastPracticeAt" label="最近练习" width="170" />
    </el-table>

    <BasePagination
      :current-page="pageNum"
      :page-size="pageSize"
      :total="total"
      @update:current-page="pageNum = $event; loadData()"
      @update:page-size="pageSize = $event; pageNum = 1; loadData()"
    />
  </el-card>

  <el-dialog v-model="exportVisible" title="导出错题" width="520px" destroy-on-close>
    <el-form label-width="120px">
      <el-form-item label="导出题目">
        <span>{{ selectedIds.length ? `已勾选 ${selectedIds.length} 题` : `当前页全部 ${tableData.length} 题` }}</span>
      </el-form-item>
      <el-form-item label="学科">
        <el-select
          v-model="exportForm.subjects"
          multiple
          clearable
          filterable
          collapse-tags
          collapse-tags-tooltip
          placeholder="不选则包含全部学科"
          style="width: 100%"
        >
          <el-option v-for="item in availableSubjects" :key="item" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="答案与解析">
        <el-radio-group v-model="exportForm.includeAnswerAnalysis">
          <el-radio :value="false">不含（模拟考试）</el-radio>
          <el-radio :value="true">包含（错题分析）</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="exportVisible = false">取消</el-button>
      <el-button type="primary" :loading="exportLoading" @click="handleExport">确认导出</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.page-desc {
  margin: 0 0 16px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
  line-height: 1.6;
}
.filter-form { margin-bottom: 16px; }
.card-header--split {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.header-title {
  font-size: 16px;
}
.header-user {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
.selection-tip {
  margin-bottom: 12px;
  color: var(--el-color-primary);
  font-size: 13px;
}
</style>
