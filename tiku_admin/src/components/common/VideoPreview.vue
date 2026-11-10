<script setup>
import { computed, ref, watch } from 'vue'
import { resolveMediaUrl } from '@/utils/image-field'

const props = defineProps({
  src: { type: String, default: '' },
  poster: { type: String, default: '' },
  /** 列表等紧凑场景 */
  compact: { type: Boolean, default: false },
})

const loadError = ref(false)

watch(
  () => props.src,
  () => {
    loadError.value = false
  },
)

const videoSrc = computed(() => resolveMediaUrl(props.src))
const posterSrc = computed(() => resolveMediaUrl(props.poster))

function handleVideoError() {
  loadError.value = true
}
</script>

<template>
  <div v-if="videoSrc" class="video-preview-wrap">
    <video
      v-show="!loadError"
      :class="compact ? 'video-preview video-preview--compact' : 'video-preview'"
      :src="videoSrc"
      :poster="posterSrc || undefined"
      controls
      preload="metadata"
      playsinline
      @error="handleVideoError"
    />
    <div v-if="loadError" :class="compact ? 'video-preview video-preview--compact video-preview--error' : 'video-preview video-preview--error'">
      <span>无法播放</span>
      <small>请重新上传讲解视频</small>
    </div>
  </div>
  <span v-else class="video-preview__empty">未配置</span>
</template>

<style scoped>
.video-preview-wrap {
  display: inline-block;
  max-width: 100%;
}

.video-preview {
  display: block;
  width: 100%;
  max-width: 640px;
  border-radius: 8px;
  background: #0f172a;
}

.video-preview--compact {
  width: 160px;
  max-width: 160px;
  height: 90px;
  object-fit: cover;
}

.video-preview--error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #fca5a5;
  font-size: 12px;
  text-align: center;
  padding: 8px;
  box-sizing: border-box;
}

.video-preview--error small {
  color: var(--el-text-color-secondary);
  font-size: 11px;
}

.video-preview__empty {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>
