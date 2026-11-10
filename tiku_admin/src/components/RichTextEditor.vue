<script setup>
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
import { ref } from 'vue'
import { uploadFileApi } from '@/api/modules/system-config'
import { getApiBaseUrl } from '@/utils/tool'
import { showError } from '@/utils/modal'

const model = defineModel({ type: String, default: '' })

defineProps({
  placeholder: { type: String, default: '请输入正文内容' },
  minHeight: { type: String, default: '280px' },
})

const quillRef = ref()
const uploading = ref(false)

/** 将上传返回路径转为可访问 URL */
function resolveFileUrl(path = '') {
  const value = String(path).trim()
  if (!value) return ''
  if (/^https?:\/\//i.test(value)) return value
  const base = getApiBaseUrl().replace(/\/$/, '')
  return `${base}/${value.replace(/^\//, '')}`
}

/** 富文本内图片走文件上传接口，禁止手填外链 URL */
async function handleImageUpload() {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  input.onchange = async () => {
    const file = input.files?.[0]
    if (!file) return
    uploading.value = true
    try {
      const res = await uploadFileApi(file)
      const url = resolveFileUrl(res?.path || res?.relativePath)
      if (!url) {
        showError('图片上传失败，未返回有效路径')
        return
      }
      const quill = quillRef.value?.getQuill?.()
      if (!quill) return
      const range = quill.getSelection(true)
      quill.insertEmbed(range.index, 'image', url)
      quill.setSelection(range.index + 1)
    } catch {
      showError('图片上传失败')
    } finally {
      uploading.value = false
    }
  }
  input.click()
}

const editorOptions = {
  theme: 'snow',
  modules: {
    toolbar: {
      container: [
        [{ header: [1, 2, 3, false] }],
        ['bold', 'italic', 'underline'],
        [{ list: 'ordered' }, { list: 'bullet' }],
        ['link', 'image'],
        ['clean'],
      ],
      handlers: {
        image: handleImageUpload,
      },
    },
  },
}
</script>

<template>
  <div v-loading="uploading" class="rich-text-editor" :style="{ '--editor-min-height': minHeight }">
    <QuillEditor
      ref="quillRef"
      v-model:content="model"
      content-type="html"
      :options="editorOptions"
      :placeholder="placeholder"
    />
  </div>
</template>

<style scoped lang="scss">
.rich-text-editor {
  :deep(.ql-container) {
    min-height: var(--editor-min-height);
    font-size: 14px;
  }

  :deep(.ql-editor) {
    min-height: var(--editor-min-height);
    line-height: 1.7;
  }
}
</style>
