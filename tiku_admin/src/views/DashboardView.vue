<script setup>
import * as echarts from 'echarts'
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getDashboardOverview } from '@/api/modules/stats'
import { BRAND_ADMIN_TITLE, BRAND_NAME } from '@/constants/brand'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const overview = ref(createEmptyOverview())

const pieUserRef = ref()
const pieQuestionRef = ref()
const piePracticeRef = ref()
const lineUserRef = ref()
const linePracticeRef = ref()
const lineVipRef = ref()

const chartInstances = []
let resizeHandler = null

const kpiCards = [
  { key: 'appUserTotal', label: 'C端用户', path: '/users', color: '#386df5' },
  { key: 'todayNewUsers', label: '今日新增', path: '/users', color: '#67c23a' },
  { key: 'vipUserTotal', label: 'VIP会员', path: '/business/vip-plans', color: '#e6a23c' },
  { key: 'questionTotal', label: '题库题目', path: '/business/questions', color: '#909399' },
  { key: 'todayPracticeCount', label: '今日练习', path: '/business/analysis', color: '#626aef' },
  { key: 'pendingWithdrawCount', label: '待审提现', path: '/business/promo-withdraw', color: '#f56c6c' },
]

const quickEntries = [
  { label: 'C端用户', path: '/users' },
  { label: '题目管理', path: '/business/questions' },
  { label: '章节题库', path: '/business/chapter-tree' },
  { label: '学情统计', path: '/business/analysis' },
  { label: '提现审核', path: '/business/promo-withdraw' },
  { label: '消息管理', path: '/business/messages' },
]

function createEmptyOverview() {
  return {
    appUserTotal: 0,
    todayNewUsers: 0,
    vipUserTotal: 0,
    questionTotal: 0,
    paperSetTotal: 0,
    todayPracticeCount: 0,
    pendingWithdrawCount: 0,
    messageUnreadCount: 0,
    weekVipAmount: 0,
    userComposition: [],
    questionTypeDistribution: [],
    practiceSourceDistribution: [],
    userRegisterTrend: [],
    practiceActiveTrend: [],
    vipOrderTrend: [],
  }
}

function goTo(path) {
  if (path) router.push(path)
}

function disposeCharts() {
  chartInstances.forEach((chart) => chart.dispose())
  chartInstances.length = 0
}

function initChart(el, option) {
  if (!el) return
  const chart = echarts.init(el)
  chart.setOption(option)
  chartInstances.push(chart)
}

function buildPieOption(title, data, colors) {
  return {
    title: { text: title, left: 'center', textStyle: { fontSize: 14, fontWeight: 600 } },
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    color: colors,
    legend: { bottom: 0, type: 'scroll' },
    series: [{
      type: 'pie',
      radius: ['38%', '62%'],
      center: ['50%', '46%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { formatter: '{b}\n{d}%' },
      data: (data || []).map((item) => ({ name: item.name, value: Number(item.value || 0) })),
    }],
  }
}

function buildLineOption(title, trend, valueKey, unit = '') {
  const dates = (trend || []).map((item) => item.date?.slice(5) || '')
  const values = (trend || []).map((item) => Number(item[valueKey] || 0))
  return {
    title: { text: title, left: 0, textStyle: { fontSize: 14, fontWeight: 600 } },
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 48, bottom: 28 },
    xAxis: { type: 'category', boundaryGap: false, data: dates },
    yAxis: { type: 'value', minInterval: valueKey === 'amount' ? undefined : 1 },
    series: [{
      type: 'line',
      smooth: true,
      showSymbol: true,
      areaStyle: { opacity: 0.12 },
      lineStyle: { width: 3 },
      data: values,
      name: unit || '数量',
    }],
  }
}

function renderCharts() {
  disposeCharts()
  const data = overview.value
  initChart(pieUserRef.value, buildPieOption('用户构成', data.userComposition, ['#386df5', '#c0c4cc']))
  initChart(pieQuestionRef.value, buildPieOption('题目题型', data.questionTypeDistribution, ['#67c23a', '#e6a23c', '#909399', '#626aef', '#f56c6c']))
  initChart(piePracticeRef.value, buildPieOption('练习来源', data.practiceSourceDistribution, ['#386df5', '#f56c6c', '#67c23a']))
  initChart(lineUserRef.value, buildLineOption('近7日新增用户', data.userRegisterTrend, 'count'))
  initChart(linePracticeRef.value, buildLineOption('近7日练习提交', data.practiceActiveTrend, 'count'))
  initChart(lineVipRef.value, buildLineOption('近7日VIP收入（元）', data.vipOrderTrend, 'amount'))
}

async function loadOverview() {
  loading.value = true
  try {
    overview.value = { ...createEmptyOverview(), ...(await getDashboardOverview()) }
    await nextTick()
    renderCharts()
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  resizeHandler = () => chartInstances.forEach((chart) => chart.resize())
  window.addEventListener('resize', resizeHandler)
  await loadOverview()
})

onBeforeUnmount(() => {
  if (resizeHandler) window.removeEventListener('resize', resizeHandler)
  disposeCharts()
})
</script>

<template>
  <div class="dashboard-page">
    <el-card shadow="never" class="page-card dashboard-hero">
      <div class="dashboard-hero__content">
        <div>
          <div class="dashboard-hero__title">{{ BRAND_ADMIN_TITLE }}</div>
          <div class="dashboard-hero__sub">欢迎回来，{{ authStore.profile?.name || '管理员' }} · 运营看板</div>
        </div>
        <el-space wrap>
          <el-tag type="info">{{ BRAND_NAME }}</el-tag>
          <el-tag type="success">近7日VIP收入 ¥{{ Number(overview.weekVipAmount || 0).toFixed(2) }}</el-tag>
          <el-tag>题集 {{ overview.paperSetTotal || 0 }}</el-tag>
        </el-space>
      </div>
    </el-card>

    <el-row v-loading="loading" :gutter="16">
      <el-col v-for="item in kpiCards" :key="item.key" :xs="12" :sm="8" :md="8" :lg="4" :xl="4">
        <el-card shadow="hover" class="stat-card" @click="goTo(item.path)">
          <div class="stat-card__value" :style="{ color: item.color }">{{ overview[item.key] ?? 0 }}</div>
          <div class="stat-card__label">{{ item.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :md="8">
        <el-card shadow="never" class="chart-card"><div ref="pieUserRef" class="chart-box" /></el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card shadow="never" class="chart-card"><div ref="pieQuestionRef" class="chart-box" /></el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card shadow="never" class="chart-card"><div ref="piePracticeRef" class="chart-box" /></el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="chart-card"><div ref="lineUserRef" class="chart-box chart-box--line" /></el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="chart-card"><div ref="linePracticeRef" class="chart-box chart-box--line" /></el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="chart-card"><div ref="lineVipRef" class="chart-box chart-box--line" /></el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="page-card">
      <template #header><strong>快捷入口</strong></template>
      <el-space wrap>
        <el-button v-for="item in quickEntries" :key="item.path" @click="goTo(item.path)">{{ item.label }}</el-button>
      </el-space>
    </el-card>
  </div>
</template>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dashboard-hero__content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.dashboard-hero__title {
  font-size: 22px;
  font-weight: 700;
}

.dashboard-hero__sub {
  margin-top: 6px;
  color: var(--el-text-color-secondary);
}

.stat-card {
  cursor: pointer;
  margin-bottom: 16px;
}

.stat-card__value {
  font-size: 30px;
  font-weight: 700;
  line-height: 1.2;
}

.stat-card__label {
  margin-top: 8px;
  color: var(--el-text-color-secondary);
}

.chart-card {
  margin-bottom: 16px;
}

.chart-box {
  height: 300px;
}

.chart-box--line {
  height: 280px;
}
</style>
