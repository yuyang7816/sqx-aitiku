<script setup>
import { computed, ref } from 'vue'
import { Document, QuestionFilled, Reading } from '@element-plus/icons-vue'
import { BUSINESS_RULES_DOC_META, BUSINESS_RULES_SECTIONS } from '@/constants/business-rules-doc'

const activeSection = ref(BUSINESS_RULES_SECTIONS[0]?.id || '23.1')
const keyword = ref('')

const currentSection = computed(() =>
  BUSINESS_RULES_SECTIONS.find((item) => item.id === activeSection.value),
)

const filteredSections = computed(() => {
  const key = keyword.value.trim().toLowerCase()
  if (!key) {
    return BUSINESS_RULES_SECTIONS
  }
  return BUSINESS_RULES_SECTIONS.filter((section) => {
    const haystack = [
      section.id,
      section.title,
      section.summary,
      ...(section.rules || []).flatMap((rule) => [
        rule.name,
        rule.userBehavior,
        rule.platformRule,
        rule.csTip,
      ]),
      ...(section.faq || []).flatMap((item) => [item.q, item.a]),
    ]
      .join(' ')
      .toLowerCase()
    return haystack.includes(key)
  })
})

function selectSection(id) {
  activeSection.value = id
}

function handleTabChange(name) {
  activeSection.value = name
}
</script>

<template>
  <div class="business-rules-page">
    <el-alert
      type="info"
      :closable="false"
      show-icon
      class="intro-alert"
      :title="`${BUSINESS_RULES_DOC_META.module} · ${BUSINESS_RULES_DOC_META.title}`"
    >
      <template #default>
        <p class="intro-text">
          面向 <strong>{{ BUSINESS_RULES_DOC_META.audience }}</strong> 的内部规则说明，覆盖文档
          <strong>23.1～23.5</strong>。{{ BUSINESS_RULES_DOC_META.remark }}
        </p>
        <p class="intro-meta">文档版本：{{ BUSINESS_RULES_DOC_META.updatedAt }}</p>
      </template>
    </el-alert>

    <el-row :gutter="16">
      <el-col :xs="24" :md="7" :lg="6">
        <el-card shadow="never" class="nav-card">
          <template #header>
            <div class="nav-header">
              <el-icon><Reading /></el-icon>
              <span>规则目录</span>
            </div>
          </template>

          <el-input
            v-model="keyword"
            clearable
            placeholder="搜索规则 / 关键词"
            class="nav-search"
          />

          <el-menu
            :default-active="activeSection"
            class="section-menu"
            @select="selectSection"
          >
            <el-menu-item
              v-for="section in filteredSections"
              :key="section.id"
              :index="section.id"
            >
              <span class="section-id">{{ section.id }}</span>
              <span class="section-title">{{ section.title }}</span>
            </el-menu-item>
          </el-menu>

          <el-empty
            v-if="filteredSections.length === 0"
            description="无匹配规则，请调整关键词"
            :image-size="64"
          />
        </el-card>
      </el-col>

      <el-col :xs="24" :md="17" :lg="18">
        <el-card v-if="currentSection" shadow="never" class="content-card">
          <template #header>
            <div class="content-header">
              <div>
                <el-tag type="primary" effect="plain" class="section-tag">{{ currentSection.id }}</el-tag>
                <strong class="content-title">{{ currentSection.title }}</strong>
              </div>
            </div>
          </template>

          <p class="section-summary">{{ currentSection.summary }}</p>

          <el-divider content-position="left">
            <el-icon><Document /></el-icon>
            规则明细
          </el-divider>

          <div
            v-for="rule in currentSection.rules"
            :key="rule.key"
            class="rule-block"
          >
            <div class="rule-head">
              <el-tag size="small" type="warning">{{ rule.key }}</el-tag>
              <strong>{{ rule.name }}</strong>
            </div>
            <el-descriptions :column="1" border size="small" class="rule-desc">
              <el-descriptions-item label="用户端表现">
                {{ rule.userBehavior }}
              </el-descriptions-item>
              <el-descriptions-item label="平台规则">
                {{ rule.platformRule }}
              </el-descriptions-item>
              <el-descriptions-item label="客服话术要点">
                <span class="cs-tip">{{ rule.csTip }}</span>
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <template v-if="currentSection.faq?.length">
            <el-divider content-position="left">
              <el-icon><QuestionFilled /></el-icon>
              常见咨询
            </el-divider>
            <el-collapse accordion class="faq-collapse">
              <el-collapse-item
                v-for="(item, index) in currentSection.faq"
                :key="`${currentSection.id}-faq-${index}`"
                :title="item.q"
                :name="String(index)"
              >
                <p class="faq-answer">{{ item.a }}</p>
              </el-collapse-item>
            </el-collapse>
          </template>

          <template v-if="currentSection.relatedPages?.length">
            <el-divider content-position="left">管理端关联页面</el-divider>
            <ul class="related-list">
              <li v-for="page in currentSection.relatedPages" :key="page">{{ page }}</li>
            </ul>
          </template>
        </el-card>
      </el-col>
    </el-row>

    <!-- 移动端：顶部 Tab 快速切换 -->
    <el-tabs
      v-model="activeSection"
      type="card"
      class="mobile-tabs"
      @tab-change="handleTabChange"
    >
      <el-tab-pane
        v-for="section in BUSINESS_RULES_SECTIONS"
        :key="section.id"
        :label="`${section.id} ${section.title}`"
        :name="section.id"
      />
    </el-tabs>
  </div>
</template>

<style scoped lang="scss">
.business-rules-page {
  .intro-alert {
    margin-bottom: 16px;
  }

  .intro-text {
    margin: 0 0 6px;
    line-height: 1.6;
    font-size: 13px;
  }

  .intro-meta {
    margin: 0;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  .nav-card {
    margin-bottom: 16px;

    .nav-header {
      display: flex;
      align-items: center;
      gap: 6px;
      font-weight: 600;
    }

    .nav-search {
      margin-bottom: 12px;
    }

    .section-menu {
      border-right: none;

      .section-id {
        display: inline-block;
        min-width: 36px;
        margin-right: 8px;
        font-weight: 600;
        color: var(--el-color-primary);
      }

      .section-title {
        font-size: 13px;
      }
    }
  }

  .content-card {
    margin-bottom: 16px;

    .content-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
    }

    .section-tag {
      margin-right: 8px;
    }

    .content-title {
      font-size: 16px;
    }

    .section-summary {
      margin: 0 0 16px;
      padding: 12px 14px;
      background: var(--el-fill-color-light);
      border-radius: 6px;
      line-height: 1.7;
      color: var(--el-text-color-regular);
      font-size: 14px;
    }
  }

  .rule-block {
    margin-bottom: 16px;

    .rule-head {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 8px;
    }

    .rule-desc {
      margin-bottom: 0;
    }

    .cs-tip {
      color: var(--el-color-success-dark-2);
    }
  }

  .faq-collapse {
    margin-bottom: 8px;

    .faq-answer {
      margin: 0;
      line-height: 1.7;
      color: var(--el-text-color-regular);
    }
  }

  .related-list {
    margin: 0;
    padding-left: 18px;
    line-height: 1.8;
    color: var(--el-text-color-regular);
    font-size: 13px;
  }

  .mobile-tabs {
    display: none;
  }

  @media (max-width: 992px) {
    .nav-card {
      display: none;
    }

    .mobile-tabs {
      display: block;
      margin-bottom: 12px;
    }
  }
}
</style>
