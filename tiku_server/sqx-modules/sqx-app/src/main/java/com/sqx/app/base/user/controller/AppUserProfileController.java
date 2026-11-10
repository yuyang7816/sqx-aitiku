package com.sqx.app.base.user.controller;

import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.user.domain.bo.UpdateAppUserProfileBO;
import com.sqx.app.base.user.delete.domain.vo.AppUserDeleteAccountVO;
import com.sqx.app.base.user.delete.service.UserAccountDeletionRequestService;
import com.sqx.app.base.user.domain.vo.AppUserProfileVO;
import com.sqx.app.base.user.service.UserService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端用户资料（与 /app/auth、/app/verifyCode 路径风格一致）
 */
@Tag(name = "用户端-用户资料", description = "文档 21.1：GET/PUT/POST /app/user 读写 profile 全字段（grade/subjects/version/ready/nickname/avatar/vip），见 docs/user-api.md")
@RestController
@RequestMapping("/app/user")
@SecurityRequirement(name = "appToken")
public class AppUserProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserAccountDeletionRequestService userAccountDeletionRequestService;

    @Operation(summary = "获取当前登录用户资料", description = "返回 userId/phone/nickname/avatar/grade/gradeShort/version/subjects/ready/vip/vipExpireAt，见 docs/user-api.md")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppUserProfileVO.class)))
    @Login
    @GetMapping
    public Result<AppUserProfileVO> getProfile(@RequestAttribute("userId") Long userId) {
        return userService.getAppUserProfile(userId);
    }

    @Operation(summary = "更新当前登录用户学习计划资料", description = "接收 grade/subjects/version/ready，可选 nickname/avatar；返回最新 AppUserProfileVO，见 docs/user-api.md")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppUserProfileVO.class)))
    @Login
    @PutMapping
    public Result<AppUserProfileVO> updateProfilePut(@RequestAttribute("userId") Long userId,
                                                     @RequestBody @Validated UpdateAppUserProfileBO updateAppUserProfileBO) {
        return userService.updateAppUserProfile(userId, updateAppUserProfileBO);
    }

    @Operation(summary = "更新当前登录用户学习计划资料（POST）", description = "与 PUT /app/user 行为一致，兼容 uniapp postJson；见 docs/user-api.md")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppUserProfileVO.class)))
    @Login
    @PostMapping
    public Result<AppUserProfileVO> updateProfilePost(@RequestAttribute("userId") Long userId,
                                                      @RequestBody @Validated UpdateAppUserProfileBO updateAppUserProfileBO) {
        return userService.updateAppUserProfile(userId, updateAppUserProfileBO);
    }

    @Operation(summary = "提交账号注销申请", description = "进入 7 天冷静期，期间保留全部数据，可申请撤销")
    @Login
    @PostMapping("/delete")
    public Result<AppUserDeleteAccountVO> deleteAccount(@RequestAttribute("userId") Long userId) {
        return userService.deleteAppAccount(userId);
    }

    @Operation(summary = "查询账号注销冷静期状态")
    @Login
    @GetMapping("/delete/status")
    public Result<AppUserDeleteAccountVO> deleteStatus(@RequestAttribute("userId") Long userId) {
        return Result.success(userAccountDeletionRequestService.getDeleteStatus(userId));
    }

    @Operation(summary = "撤销账号注销申请", description = "冷静期内撤销后账号与数据保持不变")
    @Login
    @PostMapping("/delete/cancel")
    public Result<AppUserDeleteAccountVO> cancelDeleteAccount(@RequestAttribute("userId") Long userId) {
        return Result.success(userAccountDeletionRequestService.cancelDeletion(userId));
    }
}