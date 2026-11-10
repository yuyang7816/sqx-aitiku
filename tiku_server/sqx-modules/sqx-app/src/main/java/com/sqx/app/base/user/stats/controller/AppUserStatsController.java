package com.sqx.app.base.user.stats.controller;

import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.user.stats.domain.vo.AppUserStatsVO;
import com.sqx.app.base.user.stats.service.UserStatsService;
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
 * 用户端学习统计（GET /app/user/stats，需登录）
 */
@Tag(name = "用户端-学习统计", description = "个人中心统计三栏：学习天数、做题总数、获得勋章")
@RestController
@RequestMapping("/app/user")
@SecurityRequirement(name = "appToken")
public class AppUserStatsController {

    @Resource
    private UserStatsService userStatsService;

    @Operation(summary = "累计学习统计", description = "聚合练习记录返回学习天数、做题总数与已获得勋章数")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppUserStatsVO.class)))
    @Login
    @GetMapping("/stats")
    public Result<AppUserStatsVO> stats(@RequestAttribute("userId") Long userId) {
        return Result.success(userStatsService.getStats(userId));
    }
}
