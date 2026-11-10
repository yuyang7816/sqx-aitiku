package com.sqx.app.base.help.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class FeedbackInfoBo {

    @NotNull(message = "反馈ID不能为空")
    @Schema(description = "反馈主键")
    private Long id;
}
