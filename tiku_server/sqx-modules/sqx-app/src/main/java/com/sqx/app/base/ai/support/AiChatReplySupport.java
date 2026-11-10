package com.sqx.app.base.ai.support;

import com.sqx.app.base.ai.domain.bo.AppAiChatBO;
import com.sqx.app.base.ai.domain.bo.AppAiChatHistoryItemBO;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 本地模板 AI 回复（开发/测试，逻辑对齐 uniapp chat-entry.js）
 */
public final class AiChatReplySupport {

    private static final Pattern USER_INTENT_PATTERN = Pattern.compile("请帮我|我做错|错误答案|帮我重新|帮我拆成|帮我从基础");
    private static final Pattern REPORT_PATTERN = Pattern.compile("报告|复习计划");
    private static final Pattern WEAK_PATTERN = Pattern.compile("薄弱点|基础概念");
    private static final Pattern EQUATION_PATTERN = Pattern.compile("方程");

    private static final String DEFAULT_DEMO_REPLY =
            "好的，我们来解这道题：先移项，把常数 2 留在左边，3 移到右边要变号，得到 x = 2 - 3 = -1 即可";

    private AiChatReplySupport() {
    }

    public static String buildReply(AppAiChatBO chatBO) {
        String topic = normalize(chatBO == null ? null : chatBO.getTopic());
        String message = normalize(chatBO == null ? null : chatBO.getMessage());
        List<AppAiChatHistoryItemBO> history = chatBO == null || chatBO.getHistory() == null
                ? Collections.emptyList()
                : chatBO.getHistory();

        if (StringUtils.isBlank(message)) {
            return DEFAULT_DEMO_REPLY;
        }

        if (!history.isEmpty()) {
            return buildFollowUpReply(topic, message, history);
        }

        if (isUserIntentTip(message)) {
            return buildUserIntentReply(message);
        }

        if (StringUtils.isNotBlank(topic)) {
            return "收到，我先结合题目「" + abbreviate(topic, 40)
                    + "」的考点和易错点，从思路到步骤完整讲一遍。";
        }

        if (EQUATION_PATTERN.matcher(message).find() || looksLikeEquation(message)) {
            return DEFAULT_DEMO_REPLY;
        }

        return "收到你的追问，我会结合题目背景与常见易错点，逐步帮你梳理解题思路。";
    }

    static String buildFollowUpReply(String topic, String message, List<AppAiChatHistoryItemBO> history) {
        boolean equationContext = containsEquationContext(topic, message, history);
        if (equationContext) {
            if (EQUATION_PATTERN.matcher(message).find()) {
                return "这道题目的关键是找准等量关系，先移项再合并同类项，注意变号。";
            }
            if (message.contains("为什么")) {
                return "因为移项依据的是等式的基本性质：等式两边同时加或减同一个数，等式仍然成立。";
            }
            return "把常数项移到右边要变号，再合并同类项即可得到最终答案。";
        }

        if (REPORT_PATTERN.matcher(message).find()) {
            return "建议按「薄弱章节 → 错题回顾 → 限时小测」三步执行，每步约 10 分钟，今晚 30 分钟可完成。";
        }

        return "已收到你的追问「" + abbreviate(message, 30) + "」，我会结合前面的对话继续补充说明。";
    }

    static String buildUserIntentReply(String tipText) {
        if (REPORT_PATTERN.matcher(tipText).find()) {
            return "好的，我先根据你的学习情况帮你拆一份今晚 30 分钟可执行的复习计划。";
        }
        if (WEAK_PATTERN.matcher(tipText).find()) {
            return "好的，我会结合你的薄弱点，从基础概念到典型例题帮你系统梳理一遍。";
        }
        return "收到，我先结合这道题的考点和易错点，帮你从思路到步骤完整讲一遍。";
    }

    static boolean isUserIntentTip(String text) {
        return StringUtils.isNotBlank(text) && USER_INTENT_PATTERN.matcher(text).find();
    }

    public static String resolveHistoryText(AppAiChatHistoryItemBO item) {
        if (item == null) {
            return "";
        }
        if (StringUtils.isNotBlank(item.getText())) {
            return item.getText().trim();
        }
        if (StringUtils.isNotBlank(item.getContent())) {
            return item.getContent().trim();
        }
        if (StringUtils.isNotBlank(item.getMessage())) {
            return item.getMessage().trim();
        }
        return "";
    }

    private static boolean containsEquationContext(String topic, String message,
                                                   List<AppAiChatHistoryItemBO> history) {
        if (EQUATION_PATTERN.matcher(topic).find() || EQUATION_PATTERN.matcher(message).find()
                || looksLikeEquation(topic) || looksLikeEquation(message)) {
            return true;
        }
        for (AppAiChatHistoryItemBO item : history) {
            String text = resolveHistoryText(item);
            if (EQUATION_PATTERN.matcher(text).find() || looksLikeEquation(text)) {
                return true;
            }
        }
        return false;
    }

    private static boolean looksLikeEquation(String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        }
        return text.contains("=") && text.matches(".*[a-zA-Z].*");
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private static String abbreviate(String text, int maxLen) {
        if (text.length() <= maxLen) {
            return text;
        }
        return text.substring(0, maxLen) + "…";
    }
}
