package com.sqx.app.referral.invitecode.controller;

import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.referral.invitecode.domain.vo.AppReferralInviteCodeVO;
import com.sqx.app.referral.invitecode.service.ReferralInviteCodeService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户端推广邀请码与小程序码（GET /app/referral/inviteCode，需登录）。
 * 模块十四推广海报 uniapp poster?scene=promo 通过 qrCodeUrl 展示带 scene(inviteCode) 的小程序码。
 */
@Tag(name = "用户端-推广邀请码", description = "返回唯一 inviteCode 与带 scene 的小程序码图片 URL（推广中心/推广海报共用）")
@RestController
@RequestMapping("/app/referral/inviteCode")
@SecurityRequirement(name = "appToken")
public class AppReferralInviteCodeController {

    @Resource
    private ReferralInviteCodeService referralInviteCodeService;

    @Login
    @Operation(summary = "获取推广邀请码与小程序码", description = "懒生成唯一 invitation_code，并生成 scene=inviteCode 的小程序码图片 URL")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppReferralInviteCodeVO.class)))
    @GetMapping
    public Result<AppReferralInviteCodeVO> getInviteCode(@RequestAttribute("userId") Long userId) {
        return Result.success(referralInviteCodeService.getInviteCode(userId));
    }
}
