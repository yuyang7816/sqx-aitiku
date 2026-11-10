package com.sqx.app.base.help.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 用户提交反馈（POST /app/feedback/submit）
 */
@Data
public class FeedbackSubmitBo {

    @NotBlank(message = "请选择反馈类型")
    @Schema(description = "反馈类型：feature/account/search/other")
    private String category;

    @NotBlank(message = "问题描述不能为空")
    @Size(min = 10, max = 500, message = "问题描述长度需在 10~500 字")
    @Schema(description = "问题描述")
    private String content;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "截图 URL 列表")
    private List<String> images;
}
