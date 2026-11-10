<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import AppDialog from '@/components/common/AppDialog.vue'
import BasePagination from '@/components/common/BasePagination.vue'
import TableRowActions from '@/components/common/TableRowActions.vue'
import UserLink from '@/components/business/UserLink.vue'
import RecordDetailDialog from '@/components/common/RecordDetailDialog.vue'
import {
  getAnalysisReportLogPage,
  getAnalysisSummaryInfo,
  getAnalysisSummaryPage,
} from '@/api/modules/analysis'
import {
  ANALYSIS_READ_OPTIONS,
  ANALYSIS_TABS,
  formatChangePercent,
  formatPercent,
  formatRateChange,
  getAnalysisReadLabel,
} from '@/constants/analysis'
import FilterSelect from '@/components/common/FilterSelect.vue'
import FilterSummary from '@/components/common/FilterSummary.vue'
import { useFilterFormKey } from '@/composables/useFilterFormKey'
import { FILTER_SELECT_EMPTY, normalizeNumberFilterQuery } from '@/utils/filter-form'
import { showError } from '@/utils/modal'

const activeTab = ref('summary')
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const { filterFormKey, bumpFilterFormKey } = useFilterFormKey()

const SUMMARY_FILTER_FIELDS = [
  { key: 'userName', label: '用户名', type: 'text', placeholder: '模糊匹配用户名' },
  { key: 'phone', label: '手机号', type: 'text', placeholder: '模糊匹配手机号' },
  { key: 'minWeekQuestionCount', label: '本周做题≥', type: 'text' },
  { key: 'minCorrectRate', label: '正确率≥', type: 'text' },
]

const REPORT_FILTER_FIELDS = [
  { key: 'reportMonth', label: '报告月份', type: 'text', placeholder: '如 2026-07 或 2026年7月' },
  { key: 'userName', label: '用户名', type: 'text', placeholder: '模糊匹配用户名' },
  { key: 'phone', label: '手机号', type: 'text', placeholder: '模糊匹配手机号' },
  { key: 'isRead', label: '已读状态', type: 'select', options: ANALYSIS_READ_OPTIONS, placeholder: '全部状态' },
  { key: 'dateRange', label: '推送时间', type: 'text' },
]

const summaryFilters = reactive({
  userName: '',
  phone: '',
  minWeekQuestionCount: null,
  minCorrectRate: null,
})

const reportLogFilters = reactive({
  reportMonth: '',
  userName: '',
  phone: '',
  isRead: FILTER_SELECT_EMPTY,
  dateRange: [],
})

const currentFilterFields = computed(() => (
  activeTab.value === 'summary' ? SUMMARY_FILTER_FIELDS : REPORT_FILTER_FIELDS
))
const currentFilters = computed(() => (
  activeTab.value === 'summary' ? summaryFilters : reportLogFilters
))

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref(null)

const currentTab = computed(() => ANALYSIS_TABS.find((tab) => tab.key === activeTab.value) || ANALYSIS_TABS[0])

function buildSummaryParams() {
  return {
    page: pageNum.value,
    limit: pageSize.value,
    userName: summaryFilters.userName || undefined,
    phone: summaryFilters.phone || undefined,
    minWeekQuestionCount: summaryFilters.minWeekQuestionCount ?? undefined,
    minCorrectRate: summaryFilters.minCorrectRate ?? undefined,
  }
}

function buildReportLogParams() {
  const params = {
    page: pageNum.value,
    limit: pageSize.value,
    reportMonth: reportLogFilters.reportMonth || undefined,
    userName: reportLogFilters.userName || undefined,
    phone: reportLogFilters.phone || undefined,
    isRead: normalizeNumberFilterQuery(reportLogFilters.isRead),
  }
  if (Array.isArray(reportLogFilters.dateRange) && reportLogFilters.dateRange.length === 2) {
    params.startTime = `${reportLogFilters.dateRange[0]} 00:00:00`
    params.endTime = `${reportLogFilters.dateRange[1]} 23:59:59`
  }
  return params
}

async function loadData() {
  loading.value = true
  try {
    const request = activeTab.value === 'summary'
      ? getAnalysisSummaryPage(buildSummaryParams())
      : getAnalysisReportLogPage(buildReportLogParams())
    const data = await request
    tableData.value = data.list || []
    total.value = data.total || 0
  } catch (error) {
    showError(error?.msg || error?.message || '加载学情数据失败')
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
  if (activeTab.value === 'summary') {
    Object.assign(summaryFilters, {
      userName: '',
      phone: '',
      minWeekQuestionCount: null,
      minCorrectRate: null,
    })
  } else {
    Object.assign(reportLogFilters, {
      reportMonth: '',
      userName: '',
      phone: '',
      isRead: FILTER_SELECT_EMPTY,
      dateRange: [],
    })
  }
  bumpFilterFormKey()
  pageNum.value = 1
  loadData()
}

async function openSummaryDetail(row) {
  detailVisible.value = true
  detailLoading.value = true
  detailData.value = null
  try {
    detailData.value = await getAnalysisSummaryInfo(row.userId)
  } catch (error) {
    showError(error?.msg || error?.message || '加载学情详情失败')
    detailVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

watch(activeTab, () => {
  pageNum.value = 1
  loadData()
})

onMounted(loadData)
</script>

<template>
  <el-card shadow="never">
    <template #header>
      <div>
        <strong>学情统计</strong>
        <p class="header-desc">{{ currentTab.desc }}</p>
      </div>
    </template>

    <el-tabs v-model="activeTab">
      <el-tab-pane
        v-for="tab in ANALYSIS_TABS"
        :key="tab.key"
        :label="tab.label"
        :name="tab.key"
      />
    </el-tabs>

    <el-form v-if="activeTab === 'summary'" :key="`summary-${filterFormKey}`" :inline="true" class="search-grid">
      <el-form-item label="用户名">
        <el-input v-model="summaryFilters.userName" clearable placeholder="模糊匹配用户名" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="summaryFilters.phone" clearable placeholder="模糊匹配手机号" />
      </el-form-item>
      <el-form-item label="本周做题≥">
        <el-input-number v-model="summaryFilters.minWeekQuestionCount" :min="0" :step="1" controls-position="right" />
      </el-form-item>
      <el-form-item label="正确率≥">
        <el-input-number v-model="summaryFilters.minCorrectRate" :min="0" :max="100" :step="1" controls-position="right" />
      </el-form-item>
      <el-form-item>
        <el-space>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-space>
      </el-form-item>
    </el-form>

    <el-form v-else :key="`report-${filterFormKey}`" :inline="true" class="search-grid">
      <el-form-item label="报告月份">
        <el-input v-model="reportLogFilters.reportMonth" clearable placeholder="如 2026-07 或 2026年7月" />
      </el-form-item>
      <el-form-item label="用户名">
        <el-input v-model="reportLogFilters.userName" clearable placeholder="模糊匹配用户名" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="reportLogFilters.phone" clearable placeholder="模糊匹配手机号" />
      </el-form-item>
      <el-form-item label="已读状态">
        <FilterSelect v-model="reportLogFilters.isRead" clearable placeholder="全部状态">
          <el-option
            v-for="item in ANALYSIS_READ_OPTIONS"
            :key="String(item.value)"
            :label="item.label"
            :value="item.value"
          />
        </FilterSelect>
      </el-form-item>
      <el-form-item label="推送时间">
        <el-date-picker
          v-model="reportLogFilters.dateRange"
          type="daterange"
          value-format="YYYY-MM-DD"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        />
      </el-form-item>
      <el-form-item>
        <el-space>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-space>
      </el-form-item>
    </el-form>

    <FilterSummary :filters="currentFilters" :fields="currentFilterFields" />

    <el-table
      v-if="activeTab === 'summary'"
      v-loading="loading"
      :data="tableData"
      border
      empty-text="暂无学情数据，请确认 C 端用户已有练习记录"
      class="analysis-table"
    >
      <el-table-column label="用户" min-width="140" show-overflow-tooltip>
        <template #default="{ row }">
          <div class="user-cell">
            <UserLink :user-id="row.userId" :label="row.userName || `用户${row.userId}`" />
            <span v-if="row.phone" class="user-phone">{{ row.phone }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="grade" label="年级" min-width="100" show-overflow-tooltip />
      <el-table-column label="本周做题" width="90" align="center">
        <template #default="{ row }">{{ row.weekQuestionCount ?? 0 }}</template>
      </el-table-column>
      <el-table-column label="题量变化" width="100" align="center">
        <template #default="{ row }">{{ formatChangePercent(row.weekQuestionChangePercent) }}</template>
      </el-table-column>
      <el-table-column label="正确率" width="90" align="center">
        <template #default="{ row }">{{ formatPercent(row.correctRate) }}</template>
      </el-table-column>
      <el-table-column label="正确率变化" width="110" align="center">
        <template #default="{ row }">{{ formatRateChange(row.correctRateChange) }}</template>
      </el-table-column>
      <el-table-column prop="weakPointSummary" label="薄弱点" min-width="200">
        <template #default="{ row }">
          <el-tooltip v-if="row.weakPointSummary" :content="row.weakPointSummary" placement="top">
            <span class="ellipsis-text">{{ row.weakPointSummary }}</span>
          </el-tooltip>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="90" fixed="right">
        <template #default="{ row }">
          <TableRowActions>
            <el-button link type="primary" @click="openSummaryDetail(row)">查看详情</el-button>
          </TableRowActions>
        </template>
      </el-table-column>
    </el-table>

    <el-table
      v-else
      v-loading="loading"
      :data="tableData"
      border
      empty-text="暂无报告生成日志，月报推送后此处可查询"
      class="analysis-table"
    >
      <el-table-column prop="title" label="推送标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="reportMonth" label="月份" width="100" />
      <el-table-column label="用户" min-width="140" show-overflow-tooltip>
        <template #default="{ row }">
          <div class="user-cell">
            <UserLink :user-id="row.userId" :label="row.userName || `用户${row.userId}`" />
            <span v-if="row.phone" class="user-phone">{{ row.phone }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="推送内容" min-width="240">
        <template #default="{ row }">
          <el-tooltip v-if="row.content" :content="row.content" placement="top">
            <span class="ellipsis-text">{{ row.content }}</span>
          </el-tooltip>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column label="已读" width="80">
        <template #default="{ row }">
          <el-tag :type="row.isRead === 1 ? 'success' : 'warning'" size="small">
            {{ getAnalysisReadLabel(row.isRead) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="推送时间" min-width="170" />
    </el-table>

    <BasePagination
      :current-page="pageNum"
      :page-size="pageSize"
      :total="total"
      @update:current-page="pageNum = $event; loadData()"
      @update:page-size="pageSize = $event; pageNum = 1; loadData()"
    />

    <AppDialog v-model="detailVisible" title="学情汇总详情" :show-footer="false" :loading="detailLoading">
      <template v-if="detailData">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户名">
            <UserLink
              :user-id="detailData.userId"
              :label="detailData.userName || '—'"
            />
          </el-descriptions-item>
          <el-descriptions-item label="手机号">
            <UserLink :user-id="detailData.userId" :phone="detailData.phone" />
          </el-descriptions-item>
          <el-descriptions-item label="年级">{{ detailData.grade || '—' }}</el-descriptions-item>
          <el-descriptions-item label="统计周期">
            {{ detailData.weekStartDate || '—' }} ~ {{ detailData.weekEndDate || '—' }}
          </el-descriptions-item>
          <el-descriptions-item label="本周做题">{{ detailData.weekQuestionCount ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="本周正确率">{{ formatPercent(detailData.correctRate) }}</el-descriptions-item>
          <el-descriptions-item label="今日做题">{{ detailData.todayQuestionCount ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="今日时长">{{ detailData.todayDurationMinutes ?? '—' }} 分钟</el-descriptions-item>
        </el-descriptions>

        <div class="detail-section">
          <strong>能力雷达（按学科正确率）</strong>
          <el-table :data="detailData.radarDimensions || []" border size="small" empty-text="暂无雷达数据">
            <el-table-column prop="name" label="学科" min-width="120" />
            <el-table-column label="得分" width="100">
              <template #default="{ row }">{{ formatPercent(row.score, ' 分') }}</template>
            </el-table-column>
          </el-table>
        </div>

        <div class="detail-section">
          <strong>薄弱知识点 Top3</strong>
          <el-table :data="detailData.weakPoints || []" border size="small" empty-text="暂无薄弱点">
            <el-table-column prop="title" label="章节/知识点" min-width="180" />
            <el-table-column label="正确率" width="100">
              <template #default="{ row }">{{ formatPercent(row.correctRate) }}</template>
            </el-table-column>
            <el-table-column prop="questionCount" label="做题数" width="90" />
            <el-table-column prop="action" label="推荐动作" min-width="120" />
          </el-table>
        </div>
      </template>
    </AppDialog>
  </el-card>
</template>

<style scoped>
.header-desc {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  font-weight: normal;
}

.detail-section {
  margin-top: 16px;
}

.detail-section strong {
  display: block;
  margin-bottom: 8px;
}

.analysis-table :deep(.cell) {
  line-height: 1.5;
}

.user-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.user-phone {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.ellipsis-text {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: middle;
}
</style>
