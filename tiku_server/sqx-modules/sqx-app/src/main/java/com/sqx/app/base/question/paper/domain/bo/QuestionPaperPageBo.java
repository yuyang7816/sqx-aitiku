package com.sqx.app.base.question.paper.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端题集分页查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionPaperPageBo extends PageParam {

    @Schema(description = "题集业务编码")
    private String paperCode;

    @Schema(description = "题集标题")
    private String title;

    @Schema(description = "类型：special专项 exam真题")
    private String paperType;

    @Schema(description = "年级")
    private String grade;

    @Schema(description = "学科")
    private String subject;

    @Schema(description = "状态：1启用 2禁用")
    private Integer state;
}
