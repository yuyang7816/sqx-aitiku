package com.sqx.app.base.question.knowledge.controller;

import com.sqx.app.base.question.knowledge.domain.bo.AppQuestionKnowledgeListQueryBO;
import com.sqx.app.base.question.knowledge.domain.vo.AppQuestionKnowledgePointVO;
import com.sqx.app.base.question.knowledge.service.KnowledgePointListService;
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
 * 用户端知识点练习（GET /app/question/knowledge/list，无需登录）
 */
@Tag(name = "用户端-知识点练习", description = "GET /app/question/knowledge/list 知识点列表；答题拉题见 GET /app/question/list?knowledgePoint=")
@RestController
@RequestMapping("/app/question/knowledge")
public class AppQuestionKnowledgeController {

    @Resource
    private KnowledgePointListService knowledgePointListService;

    @Operation(summary = "知识点列表", description = "按 grade/subject 可选筛选；无匹配时 data=[]")
    @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = AppQuestionKnowledgePointVO.class))))
    @GetMapping("/list")
    public Result<List<AppQuestionKnowledgePointVO>> list(@Validated AppQuestionKnowledgeListQueryBO query) {
        return Result.success(knowledgePointListService.listKnowledgePoints(query));
    }
}
