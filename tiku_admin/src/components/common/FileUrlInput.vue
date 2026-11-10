<script setup>
import { ref } from 'vue'
import { uploadFileApi } from '@/api/modules/system-config'
import { normalizeStoredMediaPath } from '@/utils/image-field'
import { showError } from '@/utils/modal'
import VideoPreview from '@/components/common/VideoPreview.vue'

const model = defineModel({ type: String, default: '' })

const props = defineProps({
  accept: { type: String, default: 'video/*,.mp4,.mov,.m4v' },
  placeholder: { type: String, default: '请上传文件' },
  buttonText: { type: String, default: '上传视频' },
  /** 仅允许通过 /file/upload 上传，禁止手填外部链接 */
  uploadOnly: { type: Boolean, default: false },
})

const uploading = ref(false)

async function handleSelectFile(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  uploading.value = true
  try {
    const res = await uploadFileApi(file)
    const pathValue = res?.relativePath || res?.path || res?.url || ''
    if (!pathValue) {
      showError('上传失败，未返回有效路径')
      return
    }
    model.value = normalizeStoredMediaPath(pathValue)
  } catch {
    showError('文件上传失败')
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

function clearValue() {
  model.value = ''
}
</script>

<template>
  <div v-loading="uploading" class="file-url-input">
    <template v-if="uploadOnly">
      <div class="file-url-input__upload-row">
        <el-button type="primary" plain @click="openFilePicker">{{ buttonText }}</el-button>
        <el-button v-if="model" @click="clearValue">移除</el-button>
      </div>
      <p v-if="!model" class="file-url-input__hint">{{ placeholder }}</p>
    </template>
    <template v-else>
      <el-input
        v-model="model"
        :placeholder="placeholder"
        clearable
        class="file-url-input__field"
      />
      <div class="file-url-input__upload-row">
        <el-button type="primary" plain @click="openFilePicker">{{ buttonText }}</el-button>
        <el-button v-if="model" @click="clearValue">清空</el-button>
      </div>
    </template>
    <VideoPreview v-if="model && (uploadOnly || /video/.test(accept))" :src="model" />
  </div>
</template>

<style scoped>
.file-url-input {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.file-url-input__field {
  width: 100%;
}

.file-url-input__upload-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.file-url-input__hint {
  margin: 0;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  word-break: break-all;
}
</style>
