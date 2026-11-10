package com.sqx.app.base.question.paper.support;

import com.alibaba.fastjson.JSON;
import com.sqx.app.base.question.paper.domain.vo.AppQuestionOptionVO;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * 题目选项 JSON 解析
 */
public final class QuestionOptionsSupport {

    private QuestionOptionsSupport() {
    }

    /**
     * 解析 tb_question.options_json 为选项列表
     */
    public static List<AppQuestionOptionVO> parseOptions(String optionsJson) {
        if (StringUtils.isBlank(optionsJson)) {
            return Collections.emptyList();
        }
        try {
            List<AppQuestionOptionVO> options = JSON.parseArray(optionsJson, AppQuestionOptionVO.class);
            return options == null ? Collections.emptyList() : options;
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }
}
