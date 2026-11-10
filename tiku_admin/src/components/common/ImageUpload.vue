<script setup>
import { computed, ref } from 'vue'
import { uploadFileApi } from '@/api/modules/system-config'
import { normalizeStoredMediaPath, resolveMediaUrl } from '@/utils/image-field'
import { showError } from '@/utils/modal'

const model = defineModel({ type: String, default: '' })

const props = defineProps({
  limit: { type: Number, default: 3 },
  accept: { type: String, default: 'image/*' },
})

const uploading = ref(false)

function resolveFileUrl(path = '') {
  return resolveMediaUrl(path)
}

const imageList = computed(() => {
  const raw = String(model.value || '').trim()
  if (!raw) return []
  return raw.split(',').map((item) => item.trim()).filter(Boolean)
})

const previewList = computed(() => imageList.value.map((path) => resolveFileUrl(path)))

function updateModel(list) {
  model.value = list.join(',')
}

function removeAt(index) {
  const next = [...imageList.value]
  next.splice(index, 1)
  updateModel(next)
}

async function handleSelectFile(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  if (props.limit > 0 && imageList.value.length >= props.limit) {
    showError(`最多上传 ${props.limit} 张图片`)
    return
  }
  uploading.value = true
  try {
    const res = await uploadFileApi(file)
    const pathValue = res?.relativePath || res?.path || res?.url || ''
    if (!pathValue) {
      showError('图片上传失败，未返回有效路径')
      return
    }
    updateModel([...imageList.value, normalizeStoredMediaPath(pathValue)])
  } catch {
    showError('图片上传失败')
  } finally {
    uploading.value = false
  }
}

function openFilePicker() {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = props.accept
  input.onchange = handleSelectFile
  input.click()
}
</script>

<template>
  <div v-loading="uploading" class="image-upload">
    <div v-if="previewList.length" class="image-upload__list">
      <div v-for="(url, index) in previewList" :key="`${url}-${index}`" class="image-upload__item">
        <el-image :src="url" fit="cover" :preview-src-list="previewList" />
        <el-button class="image-upload__remove" type="danger" circle size="small" @click="removeAt(index)">x</el-button>
      </div>
    </div>
    <el-button v-if="!limit || imageList.length < limit" type="primary" plain @click="openFilePicker">上传图片</el-button>
    <span v-if="limit" class="image-upload__hint">已上传 {{ imageList.length }}/{{ limit }}</span>
  </div>
</template>

<style scoped>
.image-upload { display: flex; flex-wrap: wrap; align-items: flex-start; gap: 12px; }
.image-upload__list { display: flex; flex-wrap: wrap; gap: 12px; }
.image-upload__item { position: relative; width: 96px; height: 96px; border-radius: 6px; overflow: hidden; border: 1px solid var(--el-border-color); }
.image-upload__item :deep(.el-image) { width: 100%; height: 100%; }
.image-upload__remove { position: absolute; top: 4px; right: 4px; min-width: 24px; width: 24px; height: 24px; padding: 0; }
.image-upload__hint { color: var(--el-text-color-secondary); font-size: 12px; line-height: 32px; }
</style>