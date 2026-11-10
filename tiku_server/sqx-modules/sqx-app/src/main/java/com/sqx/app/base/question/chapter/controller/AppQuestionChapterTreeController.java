package com.sqx.app.base.question.chapter.controller;

import com.sqx.app.base.question.chapter.domain.bo.AppQuestionChapterTreeQueryBO;
import com.sqx.app.base.question.chapter.domain.vo.AppQuestionChapterNodeVO;
import com.sqx.app.base.question.chapter.service.QuestionChapterTreeService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户端题库章节树（GET /app/question/chapter/tree，无需登录）
 */
@Tag(name = "用户端-题库章节", description = "文档 21.2：GET /app/question/chapter/tree 章→节树（grade/subject/version 业务筛选），见 docs/question-tree-api.md")
@RestController
@RequestMapping("/app/question/chapter")
public class AppQuestionChapterTreeController {

    @Resource
    private QuestionChapterTreeService questionChapterTreeService;

    @Operation(summary = "章节树", description = "按 grade/subject/version 返回启用章-节树；无匹配时 data=[]，见 docs/question-tree-api.md")
    @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = AppQuestionChapterNodeVO.class))))
    @GetMapping("/tree")
    public Result<List<AppQuestionChapterNodeVO>> tree(@Validated AppQuestionChapterTreeQueryBO query) {
        return Result.success(questionChapterTreeService.getChapterTree(query));
    }
}
