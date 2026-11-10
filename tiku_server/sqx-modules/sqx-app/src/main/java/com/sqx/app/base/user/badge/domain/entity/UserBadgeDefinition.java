package com.sqx.app.base.user.badge.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户勋章里程碑定义
 */
@Getter
@Setter
@TableName("user_badge_definition")
@Schema(name = "UserBadgeDefinition", description = "用户勋章里程碑定义")
public class UserBadgeDefinition implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int STATE_ENABLED = 1;
    public static final int STATE_DISABLED = 0;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "勋章业务编码")
    @TableField("badge_code")
    private String badgeCode;

    @Schema(description = "勋章名称")
    @TableField("name")
    private String name;

    @Schema(description = "获得条件说明")
    @TableField("condition_text")
    private String conditionText;

    @Schema(description = "规则类型")
    @TableField("rule_type")
    private String ruleType;

    @Schema(description = "解锁目标值")
    @TableField("target_value")
    private Integer targetValue;

    @Schema(description = "排序，升序")
    @TableField("sort_order")
    private Integer sortOrder;

    @Schema(description = "1=启用 0=禁用")
    @TableField("state")
    private Integer state;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
