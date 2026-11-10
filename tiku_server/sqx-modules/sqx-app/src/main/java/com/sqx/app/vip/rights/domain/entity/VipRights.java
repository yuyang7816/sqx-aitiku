package com.sqx.app.vip.rights.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会员特权
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-07 16:56:11
 */
@Getter
@Setter
@TableName("vip_rights")
@Schema(name = "VipRights", description = "会员特权")
public class VipRights implements Serializable {

    private static final long serialVersionUID = 1L;
    @Schema(description = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "图标")
    @TableField("img")
    private String img;

    @Schema(description = "名称")
    @TableField("name")
    private String name;

    @Schema(description = "权益业务键：ai/ocr/video/report/ad_free")
    @TableField("rights_code")
    private String rightsCode;

    @Schema(description = "免费版展示文案")
    @TableField("free_text")
    private String freeText;

    @Schema(description = "会员版展示文案")
    @TableField("vip_text")
    private String vipText;

    @Schema(description = "排序")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

}
