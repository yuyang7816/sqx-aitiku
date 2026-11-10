<script setup>
import SysConfigSectionView from '@/components/common/SysConfigSectionView.vue'
import {
  QUESTION_VIDEO_CONFIG_SECTIONS,
  QUESTION_VIDEO_PARAM_TYPE,
  QUESTION_VIDEO_PARAM_TYPE_DESC,
} from '@/constants/question-video-config'

const editableSections = QUESTION_VIDEO_CONFIG_SECTIONS
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <div>
          <strong>讲解视频配置</strong>
          <p class="header-desc">历史遗留的 C 端默认兜底配置；新增/编辑题目时须为每题单独配置讲解视频与解析，不再依赖此处默认视频。</p>
        </div>
      </div>
    </template>

    <SysConfigSectionView
      :sections="editableSections"
      :param-type="QUESTION_VIDEO_PARAM_TYPE"
      :param-type-desc="QUESTION_VIDEO_PARAM_TYPE_DESC"
      success-message="讲解视频配置已保存"
    />
  </el-card>
</template>

<style scoped>
.header-desc {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  font-weight: normal;
}
</style>
