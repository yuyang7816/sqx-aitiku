package com.sqx.app.base.help.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class HelpFaqSaveBo {

    @NotBlank(message = "问题不能为空")
    @Schema(description = "问题")
    private String question;

    @NotBlank(message = "答案不能为空")
    @Schema(description = "答案")
    private String answer;

    @Schema(description = "排序")
    private Integer sort;
}
