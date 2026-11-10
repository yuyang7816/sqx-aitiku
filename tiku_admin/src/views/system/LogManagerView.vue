<script setup>
import { onMounted, reactive, ref } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import FilterSummary from '@/components/common/FilterSummary.vue'
import { getSysLogPage } from '@/api/modules/sys-log'
import { isAuth } from '@/utils/tool'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const filters = reactive({ keyword: '' })

const FILTER_FIELDS = [
  { key: 'keyword', label: '关键词', type: 'text', placeholder: '用户名 / 操作' },
]

async function loadData() {
  loading.value = true
  try {
    const data = await getSysLogPage({
      page: pageNum.value,
      limit: pageSize.value,
      keyword: filters.keyword,
    })
    tableData.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

function handleReset() {
  filters.keyword = ''
  pageNum.value = 1
  loadData()
}

onMounted(loadData)
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <strong>操作日志</strong>
      </div>
    </template>
    <el-form v-if="isAuth('sys:log:list')" :inline="true" class="search-grid" @submit.prevent>
      <el-form-item label="关键词">
        <el-input v-model="filters.keyword" clearable placeholder="用户名 / 操作" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item>
        <el-space>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-space>
      </el-form-item>
    </el-form>
    <FilterSummary v-if="isAuth('sys:log:list')" :filters="filters" :fields="FILTER_FIELDS" />
    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="operation" label="操作" min-width="140" />
      <el-table-column prop="method" label="请求方法" min-width="220" show-overflow-tooltip />
      <el-table-column prop="time" label="耗时(ms)" width="100" />
      <el-table-column prop="ip" label="IP" width="140" />
      <el-table-column prop="createDate" label="创建时间" min-width="180" />
      <el-table-column prop="params" label="请求参数" min-width="240" show-overflow-tooltip />
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
