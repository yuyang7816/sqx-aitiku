package com.sqx.app.base.question.chapter.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端节分页查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionSectionPageBo extends PageParam {

    @Schema(description = "节业务编码")
    private String sectionCode;

    @Schema(description = "节标题")
    private String title;

    @Schema(description = "所属章业务编码")
    private String chapterCode;

    @Schema(description = "所属章标题")
    private String chapterTitle;

    @Schema(description = "年级（筛章）")
    private String grade;

    @Schema(description = "学科（筛章）")
    private String subject;

    @Schema(description = "教材版本（筛章）")
    private String version;

    @Schema(description = "状态：1启用 2禁用")
    private Integer state;
}
