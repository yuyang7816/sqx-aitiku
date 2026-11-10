package com.sqx.app.base.question.chapter.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端章分页查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionChapterPageBo extends PageParam {

    @Schema(description = "章业务编码")
    private String chapterCode;

    @Schema(description = "章标题")
    private String title;

    @Schema(description = "年级")
    private String grade;

    @Schema(description = "学科")
    private String subject;

    @Schema(description = "教材版本")
    private String version;

    @Schema(description = "状态：1启用 2禁用")
    private Integer state;
}
