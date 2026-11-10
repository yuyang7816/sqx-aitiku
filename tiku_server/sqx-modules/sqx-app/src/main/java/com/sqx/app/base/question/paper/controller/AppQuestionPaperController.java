package com.sqx.app.base.question.paper.controller;

import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.question.paper.domain.bo.AppQuestionPaperListQueryBO;
import com.sqx.app.base.question.paper.domain.vo.AppQuestionPaperItemVO;
import com.sqx.app.base.question.paper.domain.vo.AppQuestionPaperQuestionVO;
import com.sqx.app.base.question.paper.service.QuestionPaperService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户端专项/真题题集（GET /app/question/paper/list、GET /app/question/paper/{id}/questions，需登录）
 */
@Tag(name = "用户端-题集")
@RestController
@RequestMapping("/app/question/paper")
@SecurityRequirement(name = "appToken")
public class AppQuestionPaperController {

    @Resource
    private QuestionPaperService questionPaperService;

    @Operation(summary = "题集列表", description = "按 type=special|exam 返回启用中的专项/真题题集")
    @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = AppQuestionPaperItemVO.class))))
    @Login
    @GetMapping("/list")
    public Result<List<AppQuestionPaperItemVO>> list(@Validated AppQuestionPaperListQueryBO query) {
        return Result.success(questionPaperService.listPapers(query));
    }

    @Operation(summary = "题集组卷", description = "按 paperId（题集业务编码）返回题目集合，含选项与答案")
    @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = AppQuestionPaperQuestionVO.class))))
    @Login
    @GetMapping("/{id}/questions")
    public Result<List<AppQuestionPaperQuestionVO>> questions(@PathVariable("id") String id) {
        return Result.success(questionPaperService.listPaperQuestions(id));
    }
}
