package com.sqx.app.base.question.video.support;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.sqx.common.redis.utils.ConfigUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * 从 sys_config 读取题目讲解视频目录（app_question_video_catalog JSON）
 */
public final class QuestionVideoCatalogSupport {

    private static final String CONFIG_CATALOG = "app_question_video_catalog";
    private static final String CONFIG_DEFAULT_URL = "app_default_question_video_url";
    private static final String CONFIG_DEFAULT_TEACHER = "app_default_question_video_teacher";
    private static final String CONFIG_DEFAULT_DURATION = "app_default_question_video_duration";
    private static final String CONFIG_DEFAULT_SUMMARY = "app_default_question_video_summary";
    private static final String DEFAULT_VIDEO_URL =
            "https://qiniu-web-assets.dcloud.net.cn/unidoc/zh/2minute-demo.mp4";
    private static final String DEFAULT_TEACHER = "AI 名师";
    private static final String DEFAULT_DURATION = "02:00";
    private static final String DEFAULT_SUMMARY = "逐步拆解解题思路，帮助你掌握同类题型的通法与易错点。";

    private QuestionVideoCatalogSupport() {
    }

    public static JSONObject resolveCatalogEntry(String questionCode, String title) {
        JSONArray catalog = loadCatalog();
        if (catalog == null || catalog.isEmpty()) {
            return null;
        }
        String code = StringUtils.trimToNull(questionCode);
        String titleValue = StringUtils.trimToNull(title);
        for (int i = 0; i < catalog.size(); i++) {
            JSONObject item = catalog.getJSONObject(i);
            if (item == null) {
                continue;
            }
            String entryCode = StringUtils.trimToNull(item.getString("questionCode"));
            if (code != null && code.equals(entryCode)) {
                return item;
            }
        }
        if (titleValue != null) {
            for (int i = 0; i < catalog.size(); i++) {
                JSONObject item = catalog.getJSONObject(i);
                if (item == null) {
                    continue;
                }
                String match = StringUtils.trimToNull(item.getString("titleMatch"));
                if (match != null && titleValue.contains(match)) {
                    return item;
                }
            }
        }
        return null;
    }

    public static String defaultVideoUrl() {
        String configured = ConfigUtil.getConfigValue(CONFIG_DEFAULT_URL);
        return StringUtils.isNotBlank(configured) ? configured.trim() : DEFAULT_VIDEO_URL;
    }

    public static String defaultTeacher() {
        String configured = ConfigUtil.getConfigValue(CONFIG_DEFAULT_TEACHER);
        return StringUtils.isNotBlank(configured) ? configured.trim() : DEFAULT_TEACHER;
    }

    public static String defaultDuration() {
        String configured = ConfigUtil.getConfigValue(CONFIG_DEFAULT_DURATION);
        return StringUtils.isNotBlank(configured) ? configured.trim() : DEFAULT_DURATION;
    }

    public static String defaultSummary() {
        String configured = ConfigUtil.getConfigValue(CONFIG_DEFAULT_SUMMARY);
        return StringUtils.isNotBlank(configured) ? configured.trim() : DEFAULT_SUMMARY;
    }

    static JSONArray loadCatalog() {
        String raw = ConfigUtil.getConfigValue(CONFIG_CATALOG);
        if (StringUtils.isBlank(raw)) {
            return null;
        }
        try {
            return JSON.parseArray(raw);
        } catch (Exception ex) {
            return null;
        }
    }
}
