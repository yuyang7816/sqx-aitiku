package com.sqx.app.base.question.detail.controller;

import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.question.detail.service.QuestionDetailService;
import com.sqx.app.base.question.list.domain.vo.AppQuestionListItemVO;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户端单题详情（GET /app/question/{id}，需登录以返回做题状态）
 */
@Tag(name = "用户端-题库题目", description = "文档 21.2：GET /app/question/{id} 单题详情（id 或 question_code），见 docs/question-detail-api.md")
@RestController
@RequestMapping("/app/question")
@SecurityRequirement(name = "appToken")
public class AppQuestionDetailController {

    @Resource
    private QuestionDetailService questionDetailService;

    @Operation(summary = "单题详情", description = "按题目 id 或 question_code 返回完整题目详情及用户做题状态；不存在或已禁用返回 code=350008，见 docs/question-detail-api.md")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppQuestionListItemVO.class)))
    @Login
    @GetMapping("/{id}")
    public Result<AppQuestionListItemVO> detail(@RequestAttribute("userId") Long userId,
                                                @PathVariable("id") String id) {
        return Result.success(questionDetailService.getQuestionDetail(userId, id));
    }
}
