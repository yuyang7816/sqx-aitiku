package com.sqx.app.base.help.controller;

import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.help.domain.bo.FeedbackSubmitBo;
import com.sqx.app.base.help.domain.vo.AppFeedbackSubmitResultVO;
import com.sqx.app.base.help.service.FeedbackService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户反馈工单（POST /app/feedback/submit）
 */
@Tag(name = "用户端-帮助反馈", description = "提交产品问题反馈工单")
@RestController
@RequestMapping("/app/feedback")
@SecurityRequirement(name = "appToken")
public class AppFeedbackController {

    @Resource
    private FeedbackService feedbackService;

    @Operation(summary = "提交反馈", description = "创建反馈工单并返回 ticketNo")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppFeedbackSubmitResultVO.class)))
    @Login
    @PostMapping("submit")
    public Result<AppFeedbackSubmitResultVO> submit(@RequestAttribute("userId") Long userId,
                                                    @RequestBody @Validated FeedbackSubmitBo submitBo) {
        return Result.success(feedbackService.submit(userId, submitBo));
    }
}
