package com.sqx.app.base.user.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户端当前用户资料出参（GET /app/user）
 */
@Data
@Schema(description = "用户端 profile 出参：userId/phone/nickname/avatar/grade/gradeShort/version/subjects/ready/vip/vipExpireAt")
public class AppUserProfileVO {

    @Schema(description = "用户 ID")
    private Long userId;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "年级（如初中三年级）")
    private String grade;

    @Schema(description = "年级简称（如初三）")
    private String gradeShort;

    @Schema(description = "教材版本（如人教版）")
    private String version;

    @Schema(description = "学科列表")
    private List<String> subjects;

    @Schema(description = "资料是否已完善（uniapp 用于路由守卫）")
    private Boolean ready;

    @Schema(description = "是否会员")
    private Boolean vip;

    @Schema(description = "会员到期日（yyyy-MM-dd）")
    private String vipExpireAt;
}
