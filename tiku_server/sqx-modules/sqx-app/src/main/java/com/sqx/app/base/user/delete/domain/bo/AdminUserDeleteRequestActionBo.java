package com.sqx.app.base.user.delete.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "管理端注销申请操作")
public class AdminUserDeleteRequestActionBo {

    @NotNull(message = "申请ID不能为空")
    private Long id;

    private String adminRemark;
}
