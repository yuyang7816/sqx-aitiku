<script setup>
import { onMounted, reactive, ref } from 'vue'
import AppDialog from '@/components/common/AppDialog.vue'
import BasePagination from '@/components/common/BasePagination.vue'
import FilterSelect from '@/components/common/FilterSelect.vue'
import FilterSummary from '@/components/common/FilterSummary.vue'
import TableRowActions from '@/components/common/TableRowActions.vue'
import UserLink from '@/components/business/UserLink.vue'
import { feedbackApi } from '@/api/modules/feedback'
import {
  FEEDBACK_CATEGORY_OPTIONS,
  FEEDBACK_STATUS_OPTIONS,
  getFeedbackCategoryLabel,
  getFeedbackStatusLabel,
  getFeedbackStatusTagType,
} from '@/constants/feedback'
import { useFilterFormKey } from '@/composables/useFilterFormKey'
import { FILTER_SELECT_EMPTY, isFilterEmpty } from '@/utils/filter-form'
import { validateForm } from '@/utils/form'
import { showError, showSuccess } from '@/utils/modal'
import { getApiBaseUrl } from '@/utils/tool'
import { resolveUserDisplayName } from '@/utils/user-display'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref(null)

const replyVisible = ref(false)
const replyLoading = ref(false)
const replyFormRef = ref()
const replyTarget = ref(null)
const replyForm = reactive({
  status: 'processing',
  reply: '',
})

const { filterFormKey, bumpFilterFormKey } = useFilterFormKey()

const FILTER_FIELDS = [
  { key: 'ticketNo', label: '工单号', type: 'text', placeholder: 'FB2026...' },
  { key: 'category', label: '反馈类型', type: 'select', options: FEEDBACK_CATEGORY_OPTIONS, placeholder: '全部类型' },
  { key: 'status', label: '处理状态', type: 'select', options: FEEDBACK_STATUS_OPTIONS, placeholder: '全部状态' },
  { key: 'userName', label: '用户名', type: 'text', placeholder: '用户名/昵称' },
  { key: 'phone', label: '用户手机号', type: 'text', placeholder: '登录手机号' },
  { key: 'contactPhone', label: '联系电话', type: 'text', placeholder: '反馈填写的电话' },
]

const filters = reactive({
  ticketNo: '',
  category: FILTER_SELECT_EMPTY,
  status: FILTER_SELECT_EMPTY,
  userName: '',
  phone: '',
  contactPhone: '',
})

const replyRules = {
  reply: [{ required: true, message: '请填写回复内容', trigger: 'blur' }],
}

const DETAIL_FIELDS = [
  { key: 'ticketNo', label: '工单号' },
  { key: 'userId', label: '用户' },
  { key: 'phone', label: '用户手机号' },
  { key: 'category', label: '反馈类型', formatter: (_, data) => getFeedbackCategoryLabel(data.category) },
  { key: 'status', label: '状态', formatter: (_, data) => getFeedbackStatusLabel(data.status) },
  { key: 'contactPhone', label: '联系电话' },
  { key: 'content', label: '问题描述' },
  { key: 'reply', label: '客服回复' },
  { key: 'createTime', label: '提交时间' },
  { key: 'updateTime', label: '更新时间' },
]

function resolveFileUrl(path = '') {
  const value = String(path).trim()
  if (!value) return ''
  if (/^https?:\/\//i.test(value)) return value
  const base = getApiBaseUrl().replace(/\/$/, '')
  return `${base}/${value.replace(/^\//, '')}`
}

function resolveImagePreviewList(imageUrls = '') {
  return String(imageUrls || '')
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
    .map((path) => resolveFileUrl(path))
}

function resolveUserName(row) {
  return resolveUserDisplayName(row) || '-'
}

function resolveUserPhone(row) {
  return row?.phone || row?.userEntity?.phone || '-'
}

function buildQueryParams() {
  return {
    page: pageNum.value,
    limit: pageSize.value,
    ticketNo: filters.ticketNo || undefined,
    category: isFilterEmpty(filters.category) ? undefined : filters.category,
    status: isFilterEmpty(filters.status) ? undefined : filters.status,
    userName: filters.userName || undefined,
    phone: filters.phone || undefined,
    contactPhone: filters.contactPhone || undefined,
  }
}

async function loadData() {
  loading.value = true
  try {
    const data = await feedbackApi.page(buildQueryParams())
    tableData.value = data.list
    total.value = data.total
  } catch (error) {
    showError(error?.msg || error?.message || '加载反馈列表失败')
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
    ticketNo: '',
    category: FILTER_SELECT_EMPTY,
    status: FILTER_SELECT_EMPTY,
    userName: '',
    phone: '',
    contactPhone: '',
  })
  bumpFilterFormKey()
  pageNum.value = 1
  loadData()
}

async function openDetail(row) {
  detailVisible.value = true
  detailLoading.value = true
  detailData.value = null
  try {
    detailData.value = await feedbackApi.info(row.id)
  } catch (error) {
    showError(error?.msg || error?.message || '加载反馈详情失败')
    detailVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

function openReply(row) {
  replyTarget.value = row
  replyForm.status = row.status === 'replied' ? 'replied' : 'processing'
  replyForm.reply = row.reply || ''
  replyVisible.value = true
}

async function submitReply() {
  const valid = await validateForm(replyFormRef)
  if (!valid || !replyTarget.value) return
  replyLoading.value = true
  try {
    await feedbackApi.update({
      id: replyTarget.value.id,
      status: replyForm.status || 'replied',
      reply: replyForm.reply,
    })
    showSuccess('反馈已更新')
    replyVisible.value = false
    await loadData()
  } catch (error) {
    showError(error?.msg || error?.message || '更新反馈失败')
  } finally {
    replyLoading.value = false
  }
}

async function markProcessing(row) {
  replyLoading.value = true
  try {
    await feedbackApi.update({ id: row.id, status: 'processing' })
    showSuccess('已标记为处理中')
    await loadData()
  } catch (error) {
    showError(error?.msg || error?.message || '更新状态失败')
  } finally {
    replyLoading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <strong>用户反馈管理</strong>
      </div>
    </template>

    <p class="page-desc">
      查看用户在 App「帮助与反馈」页提交的工单，支持按类型/状态筛选，并可回复用户、更新处理状态。
    </p>

    <el-form :key="filterFormKey" :inline="true" class="filter-form" @submit.prevent="handleSearch">
      <el-form-item label="工单号">
        <el-input v-model="filters.ticketNo" clearable placeholder="FB2026..." @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="反馈类型">
        <FilterSelect v-model="filters.category" clearable placeholder="全部类型">
          <el-option
            v-for="item in FEEDBACK_CATEGORY_OPTIONS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </FilterSelect>
      </el-form-item>
      <el-form-item label="处理状态">
        <FilterSelect v-model="filters.status" clearable placeholder="全部状态">
          <el-option
            v-for="item in FEEDBACK_STATUS_OPTIONS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </FilterSelect>
      </el-form-item>
      <el-form-item label="用户名">
        <el-input v-model="filters.userName" clearable placeholder="用户名/昵称" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="用户手机号">
        <el-input v-model="filters.phone" clearable placeholder="登录手机号" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="联系电话">
        <el-input v-model="filters.contactPhone" clearable placeholder="反馈填写的电话" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
    <FilterSummary :filters="filters" :fields="FILTER_FIELDS" />

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="ticketNo" label="工单号" min-width="170" />
      <el-table-column label="用户" min-width="120">
        <template #default="{ row }">
          <UserLink :user-id="row.userId" :label="resolveUserName(row)" />
        </template>
      </el-table-column>
      <el-table-column label="手机号" width="130">
        <template #default="{ row }">{{ resolveUserPhone(row) }}</template>
      </el-table-column>
      <el-table-column label="类型" width="110">
        <template #default="{ row }">{{ getFeedbackCategoryLabel(row.category) }}</template>
      </el-table-column>
      <el-table-column prop="content" label="问题描述" min-width="220" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getFeedbackStatusTagType(row.status)">
            {{ getFeedbackStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="提交时间" min-width="170" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <TableRowActions>
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button link type="success" @click="openReply(row)">回复</el-button>
            <el-button
              v-if="row.status === 'pending'"
              link
              type="warning"
              @click="markProcessing(row)"
            >标记处理中</el-button>
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
    v-model="detailVisible"
    title="反馈详情"
    :loading="detailLoading"
    :show-footer="false"
    width="920px"
  >
    <template v-if="detailData">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="工单号">{{ detailData.ticketNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="用户">
          <UserLink :user-id="detailData.userId" :label="resolveUserName(detailData)" />
        </el-descriptions-item>
        <el-descriptions-item label="用户手机号">{{ resolveUserPhone(detailData) }}</el-descriptions-item>
        <el-descriptions-item label="反馈类型">{{ getFeedbackCategoryLabel(detailData.category) }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ getFeedbackStatusLabel(detailData.status) }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detailData.contactPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="问题描述" :span="2">{{ detailData.content || '-' }}</el-descriptions-item>
        <el-descriptions-item label="客服回复" :span="2">{{ detailData.reply || '-' }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ detailData.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailData.updateTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="resolveImagePreviewList(detailData.imageUrls).length" class="image-preview-row">
        <div class="detail-images-label">反馈截图</div>
        <div class="image-preview-list">
          <el-image
            v-for="(url, index) in resolveImagePreviewList(detailData.imageUrls)"
            :key="`${url}-${index}`"
            :src="url"
            :preview-src-list="resolveImagePreviewList(detailData.imageUrls)"
            fit="cover"
            class="preview-image"
          />
        </div>
      </div>
    </template>
  </AppDialog>

  <AppDialog
    v-model="replyVisible"
    title="回复反馈"
    :loading="replyLoading"
    @confirm="submitReply"
  >
    <el-form ref="replyFormRef" :model="replyForm" :rules="replyRules" label-width="90px">
      <el-form-item label="处理状态">
        <FilterSelect v-model="replyForm.status" placeholder="请选择状态">
          <el-option
            v-for="item in FEEDBACK_STATUS_OPTIONS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </FilterSelect>
      </el-form-item>
      <el-form-item label="回复内容" prop="reply">
        <el-input
          v-model="replyForm.reply"
          type="textarea"
          :rows="5"
          maxlength="1000"
          show-word-limit
          placeholder="填写给用户的回复说明"
        />
      </el-form-item>
    </el-form>
  </AppDialog>
</template>

<style scoped lang="scss">
.page-desc {
  margin: 0 0 16px;
  color: var(--el-text-color-secondary);
  line-height: 1.6;
}

.image-preview-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 16px;
}

.preview-image {
  width: 96px;
  height: 96px;
  border-radius: 8px;
}
</style>
