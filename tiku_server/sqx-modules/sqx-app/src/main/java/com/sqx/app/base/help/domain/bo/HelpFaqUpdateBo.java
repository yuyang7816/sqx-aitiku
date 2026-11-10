package com.sqx.app.base.help.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class HelpFaqUpdateBo {

    @NotNull(message = "ID不能为空")
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "问题")
    private String question;

    @Schema(description = "答案")
    private String answer;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "1=启用 0=禁用")
    private Integer state;
}
