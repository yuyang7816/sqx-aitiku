package com.sqx.app.base.help.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.base.user.support.AdminUserQuerySupport;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端用户反馈列表/详情
 */
@Data
public class FeedbackVo implements AdminUserQuerySupport.UserDisplayTarget {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "工单号")
    private String ticketNo;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "反馈类型")
    private String category;

    @Schema(description = "问题描述")
    private String content;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "截图URL，逗号分隔")
    private String imageUrls;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "客服回复")
    private String reply;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(description = "用户名（展示）")
    private String userName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "用户信息")
    private UserEntity userEntity;
}
