package com.sqx.app.base.user.delete.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.user.delete.domain.bo.AdminUserDeleteRequestActionBo;
import com.sqx.app.base.user.delete.domain.bo.AdminUserDeleteRequestPageBo;
import com.sqx.app.base.user.delete.domain.vo.AdminUserDeleteRequestVo;
import com.sqx.app.base.user.delete.service.UserAccountDeletionRequestService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 管理端-账号注销申请
 */
@Tag(name = "管理端-账号注销申请", description = "冷静期注销申请列表与审核")
@RestController
@RequestMapping("/admin/userDeleteRequest")
public class AdminUserDeleteRequestController {

    @Resource
    private UserAccountDeletionRequestService userAccountDeletionRequestService;

    @Operation(summary = "注销申请分页列表")
    @PostMapping("page")
    public Result<IPage<AdminUserDeleteRequestVo>> page(@RequestBody @Validated AdminUserDeleteRequestPageBo pageBo) {
        return Result.success(userAccountDeletionRequestService.adminPage(pageBo));
    }

    @Operation(summary = "撤销注销申请（保留用户数据）")
    @PostMapping("cancel")
    public Result<Void> cancel(@RequestBody @Validated AdminUserDeleteRequestActionBo actionBo) {
        userAccountDeletionRequestService.adminCancel(actionBo);
        return Result.success();
    }

    @Operation(summary = "立即执行注销（跳过剩余冷静期）")
    @PostMapping("execute")
    public Result<Void> execute(@RequestBody @Validated AdminUserDeleteRequestActionBo actionBo) {
        userAccountDeletionRequestService.adminExecuteNow(actionBo);
        return Result.success();
    }
}
