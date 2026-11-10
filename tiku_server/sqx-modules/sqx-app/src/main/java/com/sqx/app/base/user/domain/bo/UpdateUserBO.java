package com.sqx.app.base.user.domain.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author chengming
 * @since 2025/4/24
 */
@Data
public class UpdateUserBO {

    /**
     * 用户ID
     */
    @Schema(description = "用户id")
    @NotNull(message = "用户id不能为空")
    private Long userId;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "年级（如初中三年级）")
    private String grade;

    @Schema(description = "年级简称（如初三，为空时按 grade 自动推导）")
    private String gradeShort;

    @Schema(description = "教材版本（如人教版）")
    private String textbookVersion;

    @Schema(description = "学科列表")
    private List<String> subjects;

    @Schema(description = "资料是否已完善 0否 1是")
    private Integer profileReady;

    @Schema(description = "是否是会员 0否 1是")
    private Integer isVip;

    @Schema(description = "会员到期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime vipExpireAt;

    /**
     * 头像
     */
    @Schema(description = "头像")
    private String avatar;

    /**
     * 性别 1男 2女
     */
    @Schema(description = "性别 1男 2女")
    private Integer sex;

    /**
     * 年龄
     */
    @Schema(description = "年龄")
    private Integer age;

    /**
     * 微信小程序openid
     */
    @Schema(description = "微信小程序openid")

    private String openId;

    /**
     * 微信app openid
     */
    @Schema(description = "微信公众号openid")
    private String wxOpenId;

    /**
     * 密码
     */
    @Schema(description = "密码")
    private String password;

    /**
     * 苹果id
     */
    @Schema(description = "苹果id")
    private String appleId;

    /**
     * 手机类型 1安卓 2ios
     */
    @Schema(description = "手机类型 1安卓 2ios")
    private Integer sysPhone;

    /**
     * 状态 1正常 2禁用
     */
    @Schema(description = "状态 1正常 2禁用")
    private Integer status;

    /**
     * 来源 app 小程序 公众号
     */
    @Schema(description = "来源 app 小程序 公众号")
    private String platform;

    @Schema(description = "app推送id")
    private String clientId;

    /**
     * 邀请码
     */
    @Schema(description = "邀请码")
    private String invitationCode;

    @Schema(description = "一级佣金比例（空表示跟随全局默认）")
    private BigDecimal commissionRateLevel1;

    @Schema(description = "二级佣金比例（空表示跟随全局默认）")
    private BigDecimal commissionRateLevel2;
}
