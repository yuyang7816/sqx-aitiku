package com.sqx.app.base.help.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端 FAQ 列表项（GET /app/help/faq/list）
 */
@Data
public class AppHelpFaqItemVO {

    @Schema(description = "FAQ ID")
    private Long id;

    @Schema(description = "问题")
    private String question;

    @Schema(description = "答案")
    private String answer;
}
