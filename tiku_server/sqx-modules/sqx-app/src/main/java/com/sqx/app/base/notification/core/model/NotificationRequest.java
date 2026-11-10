package com.sqx.app.base.notification.core.model;

import com.sqx.app.base.utils.wx.WxMaSubscribeMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author chengming
 * @since 2025/9/9
 */
@Data
public class NotificationRequest {

    @Schema(description = "用户id")
    private Long userId;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "图片URL，逗号分隔")
    private String imageUrls;

    @Schema(description = "跳转链接")
    private String url;

    @Schema(description = "消息类型: 1=系统公告, 2=用户消息 3=系统通知")
    private Integer type;

    @Schema(description = "关联数据ID（如服务ID、订单ID等）")
    private Long dataId;

    @Schema(description = "用户cid")
    private String cid;

    @Schema(description = "消息编号")
    private String msgNo;

    @Schema(description = "微信小程序订阅消息")
    private WxMaSubscribeMessage wxMaSubscribeMessage;

    @Schema(description = "客户端类型")
    private String clientType;
}
