<script setup>
import { onMounted, reactive, ref } from 'vue'
import AppDialog from '@/components/common/AppDialog.vue'
import BasePagination from '@/components/common/BasePagination.vue'
import ImageUpload from '@/components/common/ImageUpload.vue'
import TableRowActions from '@/components/common/TableRowActions.vue'
import UserLink from '@/components/business/UserLink.vue'
import RecordDetailDialog from '@/components/common/RecordDetailDialog.vue'
import { getAppUserPage } from '@/api/modules/app-user'
import { messagesApi } from '@/api/modules/messages'
import { getDashboardOverview } from '@/api/modules/stats'
import {
  MESSAGE_JUMP_OPTIONS,
  MESSAGE_PUSH_TEMPLATES,
  MESSAGE_READ_OPTIONS,
  MESSAGE_TYPE_OPTIONS,
  getMessageJumpLabel,
  getMessageTypeLabel,
} from '@/constants/messages'
import FilterSelect from '@/components/common/FilterSelect.vue'
import FilterSummary from '@/components/common/FilterSummary.vue'
import { FILTER_SELECT_EMPTY, isFilterEmpty, normalizeNumberFilterQuery } from '@/utils/filter-form'
import { validateForm } from '@/utils/form'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { getApiBaseUrl } from '@/utils/tool'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref(null)
const formRef = ref()
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const editingId = ref(null)
const userSearching = ref(false)
const userOptions = ref([])
const unreadTotal = ref(0)

const FILTER_FIELDS = [
  { key: 'title', label: '标题', type: 'text', placeholder: '消息标题' },
  { key: 'content', label: '内容', type: 'text', placeholder: '消息内容关键词' },
  { key: 'type', label: '类型', type: 'select', options: MESSAGE_TYPE_OPTIONS, placeholder: '全部类型' },
  { key: 'url', label: '跳转页面', type: 'select', options: MESSAGE_JUMP_OPTIONS.filter((opt) => opt.value), placeholder: '全部页面' },
  { key: 'userName', label: '用户名', type: 'text', placeholder: '接收用户用户名' },
  { key: 'phone', label: '手机号', type: 'text', placeholder: '接收用户手机号' },
  { key: 'isRead', label: '已读状态', type: 'select', options: MESSAGE_READ_OPTIONS, placeholder: '全部状态' },
  { key: 'dateRange', label: '创建时间', type: 'text' },
]

const filters = reactive({
  title: '',
  content: '',
  type: FILTER_SELECT_EMPTY,
  url: FILTER_SELECT_EMPTY,
  userName: '',
  phone: '',
  isRead: FILTER_SELECT_EMPTY,
  dateRange: [],
})

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

const form = reactive({
  title: '',
  content: '',
  imageUrls: '',
  url: '',
  type: 2,
  userId: null,
  dataId: null,
  isRead: 0,
})

const selectedTemplate = ref('')

const rules = {
  title: [{ required: true, message: '请输入消息标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入消息内容', trigger: 'blur' }],
  type: [{ required: true, message: '请选择消息类型', trigger: 'change' }],
  userId: [{ required: true, message: '请选择接收用户', trigger: 'change' }],
}

function buildQueryParams() {
  const params = {
    page: pageNum.value,
    limit: pageSize.value,
    title: filters.title || undefined,
    content: filters.content || undefined,
    type: normalizeNumberFilterQuery(filters.type),
    url: isFilterEmpty(filters.url) ? undefined : filters.url,
    userName: filters.userName || undefined,
    phone: filters.phone || undefined,
    isRead: normalizeNumberFilterQuery(filters.isRead),
  }
  if (Array.isArray(filters.dateRange) && filters.dateRange.length === 2) {
    params.startTime = `${filters.dateRange[0]} 00:00:00`
    params.endTime = `${filters.dateRange[1]} 23:59:59`
  }
  return params
}

async function loadUnreadSummary() {
  try {
    const data = await getDashboardOverview()
    unreadTotal.value = Number(data?.messageUnreadCount || 0)
  } catch {
    unreadTotal.value = 0
  }
}

async function loadData() {
  loading.value = true
  try {
    const data = await messagesApi.page(buildQueryParams())
    tableData.value = data.list
    total.value = data.total
    await loadUnreadSummary()
  } finally {
    loading.value = false
  }
}

function filterUnreadOnly() {
  filters.isRead = 0
  pageNum.value = 1
  loadData()
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

function handleReset() {
  Object.assign(filters, {
    title: '',
    content: '',
    type: FILTER_SELECT_EMPTY,
    url: FILTER_SELECT_EMPTY,
    userName: '',
    phone: '',
    isRead: FILTER_SELECT_EMPTY,
    dateRange: [],
  })
  pageNum.value = 1
  loadData()
}

function resetForm(initial = {}) {
  Object.assign(form, {
    title: '',
    content: '',
    imageUrls: '',
    url: '',
    type: 2,
    userId: null,
    dataId: null,
    isRead: 0,
    ...initial,
  })
  selectedTemplate.value = ''
  userOptions.value = []
}

function openCreate() {
  editingId.value = null
  resetForm({ type: 2, isRead: 0 })
  dialogVisible.value = true
}

async function openDetail(row) {
  detailVisible.value = true
  detailLoading.value = true
  detailData.value = null
  try {
    detailData.value = await messagesApi.info(row.id)
  } catch (error) {
    showError(error?.msg || error?.message || '加载消息详情失败')
    detailVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

async function openEdit(row) {
  editingId.value = row.id
  dialogVisible.value = true
  dialogLoading.value = true
  try {
    const data = await messagesApi.info(row.id)
    resetForm({
      title: data.title || '',
      content: data.content || '',
      imageUrls: data.imageUrls || '',
      url: data.url || '',
      type: data.type ?? 2,
      userId: data.userId ?? null,
      dataId: data.dataId ?? null,
      isRead: data.isRead ?? 0,
    })
    if (data.userId) {
      userOptions.value = [{
        userId: data.userId,
        label: `${data.userName || '用户'}（${data.phone || '无手机号'}）`,
      }]
    }
  } catch (error) {
    showError(error?.msg || error?.message || '加载消息详情失败')
    dialogVisible.value = false
  } finally {
    dialogLoading.value = false
  }
}

async function searchUsers(keyword) {
  const query = String(keyword || '').trim()
  if (!query) {
    userOptions.value = []
    return
  }
  userSearching.value = true
  try {
    const isPhone = /^\d+$/.test(query)
    const data = await getAppUserPage({
      page: 1,
      limit: 20,
      username: isPhone ? undefined : query,
      phone: isPhone ? query : undefined,
    })
    userOptions.value = (data.list || []).map((user) => ({
      userId: user.userId,
      label: `${user.userName || '未命名'}（${user.phone || '无手机号'}）`,
    }))
  } finally {
    userSearching.value = false
  }
}

function applyTemplate(key) {
  const tpl = MESSAGE_PUSH_TEMPLATES.find((item) => item.key === key)
  if (!tpl) return
  form.title = tpl.title
  form.content = tpl.content
  form.url = tpl.url
  form.type = tpl.type
}

async function handleSubmit() {
  const valid = await validateForm(formRef)
  if (!valid) return
  dialogLoading.value = true
  try {
    const payload = {
      title: form.title,
      content: form.content,
      imageUrls: form.imageUrls || '',
      url: form.url || '',
      type: form.type,
      userId: form.userId,
      dataId: form.dataId || null,
      isRead: Boolean(form.isRead),
    }
    if (editingId.value) {
      await messagesApi.update({ ...payload, id: editingId.value, isRead: form.isRead })
      showSuccess('消息已更新')
    } else {
      await messagesApi.save({ ...payload, isRead: false })
      showSuccess('消息已推送，C 端铃铛未读数将增加')
    }
    dialogVisible.value = false
    await loadData()
  } catch (error) {
    showError(error?.msg || error?.message || '保存失败')
  } finally {
    dialogLoading.value = false
  }
}

async function handleDelete(row) {
  await confirmAction(`确认删除消息「${row.title}」吗？`)
  await messagesApi.delete({ id: row.id })
  showSuccess('消息已删除')
  await loadData()
}

function truncateText(text, max = 40) {
  const value = String(text || '')
  return value.length > max ? `${value.slice(0, max)}…` : value
}

onMounted(loadData)
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <div>
          <strong>消息管理</strong>
          <p class="page-desc">创建并推送系统消息至指定用户，用户可在 App 消息中心查看。</p>
        </div>
        <el-button type="primary" @click="openCreate">推送消息</el-button>
      </div>
    </template>

    <el-alert
      v-if="unreadTotal > 0"
      type="warning"
      :closable="false"
      show-icon
      class="message-reminder"
    >
      <template #title>
        当前有 {{ unreadTotal }} 条消息用户尚未阅读
      </template>
      <template #default>
        <el-button link type="primary" @click="filterUnreadOnly">查看未读消息</el-button>
      </template>
    </el-alert>

    <el-form :inline="true" class="search-grid">
      <el-form-item label="标题">
        <el-input v-model="filters.title" clearable placeholder="消息标题" />
      </el-form-item>
      <el-form-item label="内容">
        <el-input v-model="filters.content" clearable placeholder="消息内容关键词" />
      </el-form-item>
      <el-form-item label="类型">
        <FilterSelect v-model="filters.type" clearable placeholder="全部类型">
          <el-option v-for="item in MESSAGE_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </FilterSelect>
      </el-form-item>
      <el-form-item label="跳转页面">
        <FilterSelect v-model="filters.url" clearable placeholder="全部跳转">
          <el-option
            v-for="item in MESSAGE_JUMP_OPTIONS.filter((opt) => opt.value)"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </FilterSelect>
      </el-form-item>
      <el-form-item label="用户名">
        <el-input v-model="filters.userName" clearable placeholder="接收用户用户名" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="filters.phone" clearable placeholder="接收用户手机号" />
      </el-form-item>
      <el-form-item label="已读状态">
        <FilterSelect v-model="filters.isRead" clearable placeholder="全部状态">
          <el-option v-for="item in MESSAGE_READ_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </FilterSelect>
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker
          v-model="filters.dateRange"
          type="daterange"
          value-format="YYYY-MM-DD"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          clearable
        />
      </el-form-item>
      <el-form-item>
        <el-space>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-space>
      </el-form-item>
    </el-form>

    <FilterSummary :filters="filters" :fields="FILTER_FIELDS" />

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="title" label="标题" min-width="160" show-overflow-tooltip />
      <el-table-column label="内容" min-width="200">
        <template #default="{ row }">{{ truncateText(row.content) }}</template>
      </el-table-column>
      <el-table-column label="类型" width="110">
        <template #default="{ row }">{{ getMessageTypeLabel(row.type) }}</template>
      </el-table-column>
      <el-table-column label="接收用户" min-width="120" show-overflow-tooltip>
        <template #default="{ row }">
          <UserLink :user-id="row.userId" :label="row.userName || `用户${row.userId}`" />
        </template>
      </el-table-column>
      <el-table-column label="手机号" min-width="130">
        <template #default="{ row }">
          <UserLink :user-id="row.userId" :phone="row.phone" />
        </template>
      </el-table-column>
      <el-table-column label="跳转页面" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">{{ getMessageJumpLabel(row.url) }}</template>
      </el-table-column>
      <el-table-column label="消息图片" width="120">
        <template #default="{ row }">
          <template v-if="resolveImagePreviewList(row.imageUrls).length">
            <el-image
              :src="resolveImagePreviewList(row.imageUrls)[0]"
              fit="cover"
              style="width: 88px; height: 48px; border-radius: 4px"
              :preview-src-list="resolveImagePreviewList(row.imageUrls)"
            />
          </template>
          <span v-else class="text-muted">无</span>
        </template>
      </el-table-column>
      <el-table-column label="已读" width="80">
        <template #default="{ row }">
          <el-tag :type="row.isRead === 1 ? 'info' : 'warning'" size="small">
            {{ row.isRead === 1 ? '已读' : '未读' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="170" />
      <el-table-column prop="updateTime" label="更新时间" min-width="170" />
      <el-table-column label="操作" min-width="150" fixed="right">
        <template #default="{ row }">
          <TableRowActions>
            <el-button link type="primary" @click="openDetail(row)">查看详情</el-button>
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

    <AppDialog
      v-model="dialogVisible"
      :title="editingId ? '编辑消息' : '推送消息'"
      :loading="dialogLoading"
      width="640px"
      @confirm="handleSubmit"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item v-if="!editingId" label="推送模板">
          <el-select
            v-model="selectedTemplate"
            clearable
            placeholder="选择常用模板快速填充"
            @change="applyTemplate"
          >
            <el-option
              v-for="item in MESSAGE_PUSH_TEMPLATES"
              :key="item.key"
              :label="item.label"
              :value="item.key"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="100" show-word-limit placeholder="如：会员即将到期" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="4" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="消息图片">
          <ImageUpload v-model="form.imageUrls" :limit="3" />
        </el-form-item>
        <el-form-item label="跳转页面">
          <el-select v-model="form.url" clearable placeholder="选择用户点击消息后打开的页面">
            <el-option
              v-for="item in MESSAGE_JUMP_OPTIONS"
              :key="item.value || 'empty'"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="消息类型" prop="type">
          <el-select v-model="form.type">
            <el-option v-for="item in MESSAGE_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="接收用户" prop="userId">
          <el-select
            v-model="form.userId"
            filterable
            remote
            reserve-keyword
            placeholder="输入用户名或手机号搜索"
            :remote-method="searchUsers"
            :loading="userSearching"
            style="width: 100%"
          >
            <el-option
              v-for="item in userOptions"
              :key="item.userId"
              :label="item.label"
              :value="item.userId"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.url === 'notice-vip-active'" label="关联订单">
          <el-input-number v-model="form.dataId" :min="0" :controls="false" placeholder="选填，会员订单编号" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="editingId" label="已读状态">
          <el-select v-model="form.isRead">
            <el-option v-for="item in MESSAGE_READ_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-form>
    </AppDialog>
    <RecordDetailDialog
      v-model="detailVisible"
      title="消息详情"
      :loading="detailLoading"
      :data="detailData"
      :fields="[
        { key: 'title', label: '标题' },
        { key: 'content', label: '内容' },
        { key: 'type', label: '类型', formatter: (value) => getMessageTypeLabel(value) },
        { key: 'userName', label: '接收用户' },
        { key: 'phone', label: '手机号' },
        { key: 'url', label: '跳转页面', formatter: (value) => getMessageJumpLabel(value) },
        { key: 'isRead', label: '已读状态', formatter: (value) => (Number(value) === 1 ? '已读' : '未读') },
        { key: 'createTime', label: '创建时间' },
        { key: 'updateTime', label: '更新时间' },
      ]"
    />
  </el-card>
</template>

<style scoped>
.page-desc {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  font-weight: normal;
}

.message-reminder {
  margin-bottom: 16px;
}

.text-muted {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
</style>