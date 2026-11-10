<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import {
  getKefuChatMessagesApi,
  getKefuChatSessionsApi,
  getKefuUnreadCountApi,
  markKefuChatReadApi,
  replyKefuChatApi,
} from '@/api/modules/kefu-chat'
import { showError, showSuccess } from '@/utils/modal'

const loadingSessions = ref(false)
const loadingMessages = ref(false)
const sending = ref(false)
const sessions = ref([])
const messages = ref([])
const activeUserId = ref(null)
const draft = ref('')
const messageListRef = ref()
let pollTimer = 0

async function loadSessions() {
  loadingSessions.value = true
  try {
    sessions.value = await getKefuChatSessionsApi(80) || []
  } finally {
    loadingSessions.value = false
  }
}

async function loadMessages(userId) {
  if (!userId) {
    messages.value = []
    return
  }
  loadingMessages.value = true
  try {
    messages.value = await getKefuChatMessagesApi(userId) || []
    await markKefuChatReadApi(userId)
    await nextTick()
    scrollToBottom()
  } finally {
    loadingMessages.value = false
  }
}

function scrollToBottom() {
  const el = messageListRef.value
  if (el) {
    el.scrollTop = el.scrollHeight
  }
}

async function selectSession(session) {
  if (!session?.userId) return
  activeUserId.value = session.userId
  await loadMessages(session.userId)
  await loadSessions()
}

async function handleSend() {
  const content = String(draft.value || '').trim()
  if (!content || !activeUserId.value) return
  sending.value = true
  try {
    await replyKefuChatApi({ userId: activeUserId.value, content })
    draft.value = ''
    await loadMessages(activeUserId.value)
    await loadSessions()
    showSuccess('回复已发送')
  } catch (error) {
    showError(error?.message || '发送失败')
  } finally {
    sending.value = false
  }
}

function schedulePoll() {
  window.clearInterval(pollTimer)
  pollTimer = window.setInterval(async () => {
    await loadSessions()
    if (activeUserId.value) {
      await loadMessages(activeUserId.value)
    }
  }, 15000)
}

watch(activeUserId, (userId) => {
  if (userId) {
    loadMessages(userId)
  }
})

onMounted(async () => {
  await loadSessions()
  schedulePoll()
})

onBeforeUnmount(() => {
  window.clearInterval(pollTimer)
})

defineExpose({ refreshUnreadCount: getKefuUnreadCountApi })
</script>

<template>
  <div class="kefu-chat-page">
    <el-row :gutter="16" class="chat-layout">
      <el-col :xs="24" :md="8" :lg="7">
        <el-card shadow="never" class="session-card" v-loading="loadingSessions">
          <template #header>
            <div class="card-header">
              <strong>会话列表</strong>
              <el-button link type="primary" @click="loadSessions">刷新</el-button>
            </div>
          </template>
          <el-empty v-if="!sessions.length" description="暂无客服会话" />
          <div v-else class="session-list">
            <div
              v-for="item in sessions"
              :key="item.userId"
              class="session-item"
              :class="{ active: activeUserId === item.userId }"
              @click="selectSession(item)"
            >
              <div class="session-top">
                <strong>{{ item.userName || `用户${item.userId}` }}</strong>
                <span class="session-time">{{ item.lastTime || '' }}</span>
              </div>
              <div class="session-meta">
                <span class="session-phone">{{ item.phone || '—' }}</span>
                <el-badge v-if="item.unreadCount > 0" :value="item.unreadCount" :max="99" />
              </div>
              <div class="session-preview">{{ item.lastContent || '暂无消息' }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="16" :lg="17">
        <el-card shadow="never" class="message-card">
          <template #header>
            <strong>{{ activeUserId ? `与用户 ${activeUserId} 的对话` : '请选择会话' }}</strong>
          </template>

          <div v-if="!activeUserId" class="chat-empty">
            <el-empty description="从左侧选择会话开始回复" />
          </div>

          <template v-else>
            <div ref="messageListRef" v-loading="loadingMessages" class="message-list">
              <div
                v-for="item in messages"
                :key="item.id"
                class="message-row"
                :class="item.senderRole === 'staff' ? 'from-staff' : 'from-user'"
              >
                <div class="message-bubble">
                  <div class="message-role">{{ item.senderRole === 'staff' ? '客服' : '用户' }}</div>
                  <div class="message-content">{{ item.content }}</div>
                  <div class="message-time">{{ item.time }}</div>
                </div>
              </div>
            </div>

            <div class="composer">
              <el-input
                v-model="draft"
                type="textarea"
                :rows="3"
                maxlength="500"
                show-word-limit
                placeholder="输入回复内容，Enter+Ctrl 发送"
                @keydown.ctrl.enter="handleSend"
              />
              <div class="composer-actions">
                <el-button type="primary" :loading="sending" :disabled="!draft.trim()" @click="handleSend">
                  发送回复
                </el-button>
              </div>
            </div>
          </template>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped lang="scss">
.kefu-chat-page {
  .chat-layout {
    min-height: calc(100vh - 180px);
  }

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .session-card,
  .message-card {
    height: 100%;
  }

  .session-list {
    max-height: calc(100vh - 240px);
    overflow-y: auto;
  }

  .session-item {
    padding: 12px;
    border-radius: 8px;
    cursor: pointer;
    border: 1px solid var(--el-border-color-lighter);
    margin-bottom: 10px;
    transition: background 0.2s;

    &:hover,
    &.active {
      background: var(--el-color-primary-light-9);
      border-color: var(--el-color-primary-light-5);
    }
  }

  .session-top,
  .session-meta {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
  }

  .session-time,
  .session-phone,
  .session-preview {
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  .session-preview {
    margin-top: 6px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .chat-empty {
    min-height: 420px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .message-list {
    min-height: 420px;
    max-height: calc(100vh - 340px);
    overflow-y: auto;
    padding: 8px 4px 16px;
    background: var(--el-fill-color-lighter);
    border-radius: 8px;
  }

  .message-row {
    display: flex;
    margin-bottom: 12px;

    &.from-staff {
      justify-content: flex-end;
    }
  }

  .message-bubble {
    max-width: 72%;
    padding: 10px 12px;
    border-radius: 10px;
    background: #fff;
    box-shadow: 0 1px 4px rgba(15, 23, 42, 0.06);
  }

  .from-staff .message-bubble {
    background: var(--el-color-primary-light-9);
  }

  .message-role,
  .message-time {
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  .message-content {
    margin: 4px 0;
    line-height: 1.6;
    word-break: break-word;
  }

  .composer {
    margin-top: 16px;
  }

  .composer-actions {
    margin-top: 10px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
