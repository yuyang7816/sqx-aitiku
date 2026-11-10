package com.sqx.app.base.user.badge.controller;

import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.user.badge.domain.vo.AppUserBadgeListVO;
import com.sqx.app.base.user.badge.service.UserBadgeService;
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
 * 用户端勋章（GET /app/user/badges，需登录）
 */
@Tag(name = "用户端-勋章", description = "练习里程碑勋章列表与获得条件；达成条件后 unlocked=true")
@RestController
@RequestMapping("/app/user")
@SecurityRequirement(name = "appToken")
public class AppUserBadgeController {

    @Resource
    private UserBadgeService userBadgeService;

    @Operation(summary = "勋章列表", description = "返回全部勋章及获得条件；基于 tb_user_practice_record 判定是否已解锁")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppUserBadgeListVO.class)))
    @Login
    @GetMapping("/badges")
    public Result<AppUserBadgeListVO> badges(@RequestAttribute("userId") Long userId) {
        return Result.success(userBadgeService.listBadges(userId));
    }
}
