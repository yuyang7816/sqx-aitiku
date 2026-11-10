package com.sqx.app.referral.members.controller;

import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.referral.members.domain.vo.AppReferralMemberVO;
import com.sqx.app.referral.members.service.ReferralMembersService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户端推广下级成员（GET /app/referral/members，需登录）
 */
@Tag(name = "用户端-推广下级成员", description = "返回当前邀请人的直接下级列表，含展示名、注册日期与付费状态")
@RestController
@RequestMapping("/app/referral/members")
@SecurityRequirement(name = "appToken")
public class AppReferralMembersController {

    @Resource
    private ReferralMembersService referralMembersService;

    @Login
    @Operation(summary = "推广下级成员列表", description = "按 superior_invitation_code 匹配当前用户 invitation_code，返回直接/间接下级；level=1 一级，level=2 二级；withCommission=1 附带分佣金额")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppReferralMemberVO.class)))
    @GetMapping
    public Result<List<AppReferralMemberVO>> list(
            @RequestAttribute("userId") Long userId,
            @RequestParam(value = "level", defaultValue = "1") Integer level,
            @RequestParam(value = "withCommission", defaultValue = "0") Integer withCommission) {
        int normalizedLevel = level != null && level == 2 ? 2 : 1;
        boolean includeCommission = withCommission != null && withCommission == 1;
        return Result.success(referralMembersService.listMembers(userId, normalizedLevel, includeCommission));
    }
}
