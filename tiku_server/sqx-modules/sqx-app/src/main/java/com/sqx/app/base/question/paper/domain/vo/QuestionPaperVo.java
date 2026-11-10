package com.sqx.app.base.question.paper.domain.vo;

import com.sqx.system.db.entity.QuestionPaper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 管理端题集 VO
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionPaperVo extends QuestionPaper {

    @Schema(description = "绑定题目列表")
    private List<QuestionPaperItemVo> items;
}
