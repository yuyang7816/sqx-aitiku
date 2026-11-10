package com.sqx.app.base.question.list.support;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * 题目标签 JSON 解析
 */
public final class QuestionTagsSupport {

    private QuestionTagsSupport() {
    }

    /**
     * 解析 tb_question.tags_json 为标签列表
     */
    public static List<String> parseTags(String tagsJson) {
        if (StringUtils.isBlank(tagsJson)) {
            return Collections.emptyList();
        }
        try {
            List<String> tags = JSON.parseArray(tagsJson, String.class);
            return tags == null ? Collections.emptyList() : tags;
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }
}
