package com.sqx.app.base.messages.domain.bo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.sqx.common.mybatis.domain.PageParam ;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-15 16:28:04
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MessagesPageBo extends PageParam {

    @Schema(description = "标题")
    private String title;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "消息类型: 1=系统公告, 2=用户消息")
    @TableField("type")
    private Integer type;

    @Schema(description = "接收用户ID")
    private Long userId;

    @Schema(description = "是否已读: 0=未读, 1=已读")
    private Integer isRead;

    @Schema(description = "起始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "跳转链接（精确匹配，如 notice-report）")
    private String url;
}
