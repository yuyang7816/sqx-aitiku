<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const pageTitle = computed(() => route.query.title || '页面不存在')
const menuUrl = computed(() => route.query.menuUrl || route.path)

function goDashboard() {
  router.push('/dashboard')
}
</script>

<template>
  <el-card shadow="never" class="page-card">
    <div class="not-found">
      <div class="not-found__code">404</div>
      <strong>{{ pageTitle }}</strong>
      <p>当前菜单已返回，但前端暂未实现对应页面。</p>
      <el-descriptions border :column="1" class="not-found__meta">
        <el-descriptions-item label="菜单地址">{{ menuUrl }}</el-descriptions-item>
      </el-descriptions>
      <el-space>
        <el-button type="primary" @click="goDashboard">返回工作台</el-button>
        <el-button @click="$router.back()">返回上一页</el-button>
      </el-space>
    </div>
  </el-card>
</template>
