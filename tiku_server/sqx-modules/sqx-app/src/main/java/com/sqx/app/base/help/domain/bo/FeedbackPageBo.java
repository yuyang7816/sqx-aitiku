package com.sqx.app.base.help.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class FeedbackPageBo extends PageParam {

    @Schema(description = "工单号")
    private String ticketNo;

    @Schema(description = "反馈类型：feature/account/search/other")
    private String category;

    @Schema(description = "状态：pending/processing/replied")
    private String status;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名关键词")
    private String userName;

    @Schema(description = "用户手机号")
    private String phone;

    @Schema(description = "反馈联系电话")
    private String contactPhone;
}
