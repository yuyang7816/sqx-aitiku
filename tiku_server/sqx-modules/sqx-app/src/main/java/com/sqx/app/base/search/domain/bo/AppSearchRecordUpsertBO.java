package com.sqx.app.base.search.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 用户端写入搜题记录入参（POST /app/search/records）
 */
@Getter
@Setter
@Schema(description = "写入搜题记录入参")
public class AppSearchRecordUpsertBO {

    @NotBlank(message = "题干预览不能为空")
    @Size(max = 512, message = "题干预览不能超过512字")
    @Schema(description = "题干预览", required = true)
    private String title;

    @NotBlank(message = "来源不能为空")
    @Schema(description = "来源：拍照搜题/手动搜题/AI答疑", required = true)
    private String source;

    @Size(max = 512, message = "原图地址不能超过512字")
    @Schema(description = "原图地址（拍照搜题可选）")
    private String imageUrl;
}
