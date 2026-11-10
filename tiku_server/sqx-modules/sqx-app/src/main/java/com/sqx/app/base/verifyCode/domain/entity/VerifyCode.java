package com.sqx.app.base.verifyCode.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 验证码
 *
 * @author sqx
 * @since 2025-04-22 15:31:50
 */
@Getter
@Setter
@TableName("verify_code")
@Schema(name = "VerifyCode", description = "验证码")
public class VerifyCode implements Serializable {

    private static final long serialVersionUID = 1L;
    @Schema(description = "id")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "手机号")
    @TableField("phone")
    private String phone;

    @Schema(description = "验证码")
    @TableField("code")
    private String code;

    @Schema(description = "状态 1: 未使用 2: 已使用")
    @TableField("status")
    private Integer status;

    @Schema(description = "过期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}
