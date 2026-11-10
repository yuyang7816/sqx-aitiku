package com.sqx.app.base.articleCms.constants;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 协议与政策 CMS：文章类型与 sys_config.param_key 映射（param_type=app_article）
 */
public final class ArticleCmsConstant {

    private ArticleCmsConstant() {
    }

    /**
     * 单篇文章类型定义
     */
    public static final class ArticleTypeDef {

        private final String type;
        private final String titleKey;
        private final String subKey;
        private final String contentKey;
        private final String defaultTitle;
        private final String defaultSub;
        private final String defaultContent;

        public ArticleTypeDef(String type, String titleKey, String subKey, String contentKey,
                              String defaultTitle, String defaultSub, String defaultContent) {
            this.type = type;
            this.titleKey = titleKey;
            this.subKey = subKey;
            this.contentKey = contentKey;
            this.defaultTitle = defaultTitle;
            this.defaultSub = defaultSub;
            this.defaultContent = defaultContent;
        }

        public String getType() {
            return type;
        }

        public String getTitleKey() {
            return titleKey;
        }

        public String getSubKey() {
            return subKey;
        }

        public String getContentKey() {
            return contentKey;
        }

        public String getDefaultTitle() {
            return defaultTitle;
        }

        public String getDefaultSub() {
            return defaultSub;
        }

        public String getDefaultContent() {
            return defaultContent;
        }
    }

    private static final Map<String, ArticleTypeDef> TYPE_MAP;

    static {
        Map<String, ArticleTypeDef> map = new LinkedHashMap<>();
        map.put("agreement", new ArticleTypeDef(
                "agreement",
                "app_article_agreement_title",
                "app_article_agreement_sub",
                "app_article_agreement_content",
                "用户协议",
                "请在使用平台服务前仔细阅读以下协议内容。",
                "<h3>账号使用</h3><p>用户应确保登录信息真实有效，不得转借、出租或共享会员权益给他人。</p>"
                        + "<h3>服务说明</h3><p>平台提供的题目解析与 AI 建议仅作为辅助学习参考，具体学习安排需结合个人实际情况。</p>"
                        + "<h3>行为规范</h3><p>禁止利用平台从事违法违规活动，包括但不限于批量抓取内容、恶意攻击和传播不当信息。</p>"
        ));
        map.put("privacy", new ArticleTypeDef(
                "privacy",
                "app_article_privacy_title",
                "app_article_privacy_sub",
                "app_article_privacy_content",
                "隐私政策",
                "我们重视你的个人信息安全，并尽量以最小化方式处理必要数据。",
                "<h3>信息收集</h3><p>为实现登录、学习记录、收藏和消息提醒等功能，平台会收集必要的账户和使用数据。</p>"
                        + "<h3>信息使用</h3><p>收集的数据仅用于学习服务优化、功能实现和必要的运营通知，不会超范围使用。</p>"
                        + "<h3>信息保护</h3><p>我们会采取合理的技术和管理措施保护数据安全，并支持用户查看、更新和删除相关信息。</p>"
        ));
        map.put("about", new ArticleTypeDef(
                "about",
                "app_article_about_title",
                "app_article_about_sub",
                "app_article_about_content",
                "关于我们",
                "AI 题库，专注中小学智能刷题与错题提升的学习助手。",
                "<h3>产品理念</h3><p>我们相信 AI 不仅是工具，更是懂你学习节奏、陪你稳步提升的智能伙伴。</p>"
                        + "<h3>核心能力</h3><p>拍照搜题、智能答疑、错题整理、学情分析、周计划与推广激励，覆盖学习全场景。</p>"
                        + "<h3>服务承诺</h3><p>持续更新题库与解析质量，保护用户学习数据安全，提供稳定流畅的使用体验。</p>"
                        + "<h3>联系我们</h3><p>如有任何问题或建议，欢迎通过「帮助与反馈」或 AI 在线客服随时联系。</p>"
                        + "<p><strong>当前版本</strong>：1.0.0</p>"
        ));
        TYPE_MAP = Collections.unmodifiableMap(map);
    }

    public static ArticleTypeDef findByType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return null;
        }
        return TYPE_MAP.get(type.trim());
    }
}
