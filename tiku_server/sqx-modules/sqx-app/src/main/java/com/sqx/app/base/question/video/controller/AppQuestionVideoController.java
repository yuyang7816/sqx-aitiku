package com.sqx.app.base.question.video.controller;

import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.question.video.domain.vo.AppQuestionVideoVO;
import com.sqx.app.base.question.video.service.QuestionVideoService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户端题目讲解视频（GET /app/question/video，需登录且为 VIP 权益）
 */
@Tag(name = "用户端-题目视频", description = "会员高清视频解析")
@RestController
@RequestMapping("/app/question")
@SecurityRequirement(name = "appToken")
public class AppQuestionVideoController {

    @Resource
    private QuestionVideoService questionVideoService;

    @Operation(summary = "题目讲解视频", description = "按 questionCode 或 questionId 返回讲解视频地址")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppQuestionVideoVO.class)))
    @Login
    @GetMapping("/video")
    public Result<AppQuestionVideoVO> video(@RequestParam(value = "questionCode", required = false) String questionCode,
                                            @RequestParam(value = "questionId", required = false) Long questionId) {
        return Result.success(questionVideoService.getQuestionVideo(questionCode, questionId));
    }
}
