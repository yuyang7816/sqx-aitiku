package com.sqx.app.base.user.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 用户端更新学习计划资料入参（PUT/POST /app/user）
 */
@Data
@Schema(description = "用户端 profile 更新入参：grade/version/subjects/ready 必填；nickname/avatar 可选")
public class UpdateAppUserProfileBO {

    @Schema(description = "年级（如初中三年级）")
    @NotBlank(message = "年级不能为空")
    private String grade;

    @Schema(description = "教材版本（如人教版）")
    @NotBlank(message = "教材版本不能为空")
    private String version;

    @Schema(description = "学科列表（至少一门）")
    @NotEmpty(message = "请至少选择一门学科")
    private List<String> subjects;

    @Schema(description = "资料是否已完善（uniapp 计划页确认后为 true）")
    @NotNull(message = "资料完善状态不能为空")
    private Boolean ready;

    @Schema(description = "昵称（头像昵称编辑页提交）")
    private String nickname;

    @Schema(description = "头像 URL（先走文件上传，再提交服务端存储地址）")
    private String avatar;
}
