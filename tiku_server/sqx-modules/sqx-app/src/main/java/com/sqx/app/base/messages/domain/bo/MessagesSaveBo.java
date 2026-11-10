package com.sqx.app.base.messages.domain.bo;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-15 16:28:04
 */
@Data
public class MessagesSaveBo {

    @Schema(description = "标题")
    private String title;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "图片URL，逗号分隔")
    private String imageUrls;

    @Schema(description = "跳转链接")
    private String url;

    @Schema(description = "消息类型: 1=系统公告, 2=用户消息")
    private Byte type;

    @Schema(description = "接收用户ID")
    private Long userId;

    @Schema(description = "关联数据ID（如服务ID、订单ID等）")
    private Long dataId;

    @Schema(description = "是否已读: 0=未读, 1=已读")
    private Boolean isRead;

}
