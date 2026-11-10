package com.sqx.app.base.help.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class HelpFaqPageBo extends PageParam {

    @Schema(description = "问题关键词")
    private String question;

    @Schema(description = "1=启用 0=禁用")
    private Integer state;
}
