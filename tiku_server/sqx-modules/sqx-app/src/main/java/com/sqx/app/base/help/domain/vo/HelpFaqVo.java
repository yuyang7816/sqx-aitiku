package com.sqx.app.base.help.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端 FAQ 列表项
 */
@Data
public class HelpFaqVo {

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

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
