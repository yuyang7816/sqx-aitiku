package com.sqx.app.base.question.list.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.question.list.domain.bo.AppQuestionListQueryBO;
import com.sqx.app.base.question.list.domain.vo.AppQuestionListItemVO;
import com.sqx.app.base.question.list.service.QuestionListService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户端练习题目拉取（GET /app/question/list，需登录以返回做题状态与完整题目详情）
 */
@Tag(name = "用户端-题库题目", description = "文档 21.2：GET /app/question/list 分页题目（chapterId/paperId/questionIds + mode），见 docs/question-list-api.md")
@RestController
@RequestMapping("/app/question")
@SecurityRequirement(name = "appToken")
public class AppQuestionListController {

    @Resource
    private QuestionListService questionListService;

    @Operation(summary = "练习题目分页列表", description = "按 chapterId/paperId/questionIds 分页返回题目详情及做题状态；mode=all|wrong|single；无匹配时 records=[]，见 docs/question-list-api.md")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppQuestionListItemVO.class)))
    @Login
    @GetMapping("/list")
    public Result<IPage<AppQuestionListItemVO>> list(@RequestAttribute("userId") Long userId,
                                                     @Validated AppQuestionListQueryBO query) {
        return Result.success(questionListService.pageQuestionList(userId, query));
    }
}
