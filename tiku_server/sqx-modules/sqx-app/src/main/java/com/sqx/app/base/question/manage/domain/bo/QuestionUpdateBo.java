package com.sqx.app.base.question.manage.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * 管理端更新题目
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionUpdateBo extends QuestionSaveBo {

    @Schema(description = "主键")
    @NotNull(message = "id不能为空")
    private Long id;
}
