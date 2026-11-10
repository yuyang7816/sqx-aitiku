package com.sqx.app.base.search.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.search.domain.bo.AppQuestionSearchQueryBO;
import com.sqx.app.base.search.domain.vo.AppQuestionSearchItemVO;
import com.sqx.app.base.search.service.QuestionSearchService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户端题库关键词搜索（GET /app/search/questions，需登录）
 */
@Tag(name = "用户端-题库搜索", description = "首页关键词检索题目/知识点")
@RestController
@RequestMapping("/app/search")
@SecurityRequirement(name = "appToken")
public class AppQuestionSearchController {

    @Resource
    private QuestionSearchService questionSearchService;

    @Operation(summary = "关键词搜索题目", description = "按题干 LIKE 分页返回启用中的题目摘要")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppQuestionSearchItemVO.class)))
    @Login
    @GetMapping("/questions")
    public Result<IPage<AppQuestionSearchItemVO>> search(@Validated AppQuestionSearchQueryBO query) {
        return Result.success(questionSearchService.searchQuestions(query));
    }
}
