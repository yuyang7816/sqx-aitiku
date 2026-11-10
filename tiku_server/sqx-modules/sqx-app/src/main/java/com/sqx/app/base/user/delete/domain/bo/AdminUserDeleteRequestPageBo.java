package com.sqx.app.base.user.delete.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "管理端注销申请分页")
public class AdminUserDeleteRequestPageBo extends PageParam {

    private Long userId;
    private String phone;
    private String userName;
    private String status;
}
