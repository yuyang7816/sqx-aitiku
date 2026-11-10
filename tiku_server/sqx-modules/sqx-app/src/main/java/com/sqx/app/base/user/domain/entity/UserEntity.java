package com.sqx.app.base.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * 用户
 *
 */
@Data
@Schema(description = "用户")
@TableName("tb_user")
public class UserEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @Schema(description = "用户id")
    @TableId(type = IdType.AUTO)
    private Long userId;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    @TableField("user_name")
    private String userName;

    /**
     * 昵称（用户端展示名，为空时回退 userName）
     */
    @Schema(description = "昵称")
    private String nickname;

    /**
     * 年级（如初中三年级）
     */
    @Schema(description = "年级")
    private String grade;

    /**
     * 年级简称（如初三）
     */
    @Schema(description = "年级简称")
    @TableField("grade_short")
    private String gradeShort;

    /**
     * 教材版本（如人教版）
     */
    @Schema(description = "教材版本")
    @TableField("textbook_version")
    private String textbookVersion;

    /**
     * 学科列表 JSON 数组（如 ["数学","物理"]）
     */
    @Schema(description = "学科列表 JSON")
    private String subjects;

    /**
     * 资料是否已完善 0否 1是
     */
    @Schema(description = "资料是否已完善 0否 1是")
    @TableField("profile_ready")
    private Integer profileReady;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    private String phone;

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
    @Schema(description = "用户端小程序openId")
    @TableField("ma_open_id")
    private String maOpenId;

    /**
     * 微信app openid
     */
    @Schema(description = "用户端公众号openId")
    @TableField("mp_open_id")
    private String mpOpenId;

    /**
     * 密码
     */
    @Schema(description = "密码")
    @JsonIgnore
    private String password;

    /**
     * 苹果id
     */
    @Schema(description = "苹果id")
    @TableField("apple_id")
    private String appleId;

    /**
     * 手机类型 1安卓 2ios
     */
    @Schema(description = "手机类型 1安卓 2ios")
    @TableField("sys_phone")
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
    private String cid;

    @Schema(description = "用户类型 1普通用户 2vip用户")
    private Integer userType;

    @Schema(description = "是否是会员(0不是 1是)")
    private Integer isVip;

    @Schema(description = "会员到期时间（续费叠加）")
    @TableField("vip_expire_at")
    private LocalDateTime vipExpireAt;

    @Schema(description = "今日已用 OCR 搜题次数")
    @TableField("ocr_used_today")
    private Integer ocrUsedToday;

    @Schema(description = "OCR 次数统计日期（跨日重置 ocr_used_today）")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @TableField("ocr_quota_date")
    private LocalDate ocrQuotaDate;

    @Schema(description = "今日已用 AI 答疑次数")
    @TableField("ai_used_today")
    private Integer aiUsedToday;

    @Schema(description = "AI 次数统计日期（跨日重置 ai_used_today）")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @TableField("ai_quota_date")
    private LocalDate aiQuotaDate;

    /**
     * 邀请码
     */
    @Schema(description = "邀请码")
    @TableField("invitation_code")
    private String invitationCode;

    /**
     * 上级邀请码
     */
    @Schema(description = "上级邀请码")
    @TableField("superior_invitation_code")
    private String superiorInvitationCode;

    @Schema(description = "一级佣金比例（空则使用全局默认，如 0.10 表示 10%）")
    @TableField("commission_rate_level1")
    private BigDecimal commissionRateLevel1;

    @Schema(description = "二级佣金比例（空则使用全局默认）")
    @TableField("commission_rate_level2")
    private BigDecimal commissionRateLevel2;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
