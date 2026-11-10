package com.sqx.app.base.question.paper.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.constants.BaseConstant;
import com.sqx.app.base.question.paper.domain.bo.*;
import com.sqx.app.base.question.paper.domain.vo.QuestionPaperItemVo;
import com.sqx.app.base.question.paper.domain.vo.QuestionPaperQuestionOptionVo;
import com.sqx.app.base.question.paper.domain.vo.QuestionPaperVo;
import com.sqx.app.base.question.paper.service.QuestionPaperAdminService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 管理端专项/真题题集
 */
@Tag(name = "管理端-题集", description = "管理端维护 tb_question_paper，供 GET /app/question/paper/list 读取")
@RestController
@RequestMapping(BaseConstant.BASE_ADMIN_PREFIX + "/question/paper")
public class AdminQuestionPaperController {

    @Resource
    private QuestionPaperAdminService questionPaperAdminService;

    @Operation(summary = "新增题集")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated QuestionPaperSaveBo saveBo) {
        questionPaperAdminService.saveInfo(saveBo);
        return Result.success();
    }

    @Operation(summary = "题集分页列表")
    @ApiResponse(content = @Content(schema = @Schema(implementation = QuestionPaperVo.class)))
    @PostMapping("page")
    public Result<IPage<QuestionPaperVo>> page(@RequestBody @Validated QuestionPaperPageBo pageBo) {
        return Result.success(questionPaperAdminService.pageInfo(pageBo));
    }

    @Operation(summary = "题集详情")
    @ApiResponse(content = @Content(schema = @Schema(implementation = QuestionPaperVo.class)))
    @PostMapping("info")
    public Result<QuestionPaperVo> info(@RequestBody @Validated QuestionPaperInfoBo infoBo) {
        return Result.success(questionPaperAdminService.info(infoBo));
    }

    @Operation(summary = "更新题集")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated QuestionPaperUpdateBo updateBo) {
        questionPaperAdminService.updateInfo(updateBo);
        return Result.success();
    }

    @Operation(summary = "删除题集")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated QuestionPaperInfoBo infoBo) {
        questionPaperAdminService.delete(infoBo);
        return Result.success();
    }

    @Operation(summary = "修改题集上下架")
    @PostMapping("updateState")
    public Result<Void> updateState(@RequestBody @Validated QuestionPaperUpdateStateBo updateStateBo) {
        questionPaperAdminService.updateState(updateStateBo);
        return Result.success();
    }

    @Operation(summary = "搜索可绑定题目")
    @ApiResponse(content = @Content(schema = @Schema(implementation = QuestionPaperQuestionOptionVo.class)))
    @PostMapping("searchQuestions")
    public Result<IPage<QuestionPaperQuestionOptionVo>> searchQuestions(
            @RequestBody @Validated QuestionPaperQuestionSearchBo searchBo) {
        return Result.success(questionPaperAdminService.searchQuestions(searchBo));
    }

    @Operation(summary = "题集绑定题目分页")
    @ApiResponse(content = @Content(schema = @Schema(implementation = QuestionPaperItemVo.class)))
    @PostMapping("pageItems")
    public Result<IPage<QuestionPaperItemVo>> pageItems(@RequestBody @Validated QuestionPaperItemPageBo pageBo) {
        return Result.success(questionPaperAdminService.pageItems(pageBo));
    }

    @Operation(summary = "题集批量绑定题目")
    @PostMapping("bindQuestions")
    public Result<Void> bindQuestions(@RequestBody @Validated QuestionPaperBindBo bindBo) {
        questionPaperAdminService.bindQuestions(bindBo);
        return Result.success();
    }

    @Operation(summary = "题集解绑题目")
    @PostMapping("unbindQuestion")
    public Result<Void> unbindQuestion(@RequestBody @Validated QuestionPaperUnbindBo unbindBo) {
        questionPaperAdminService.unbindQuestion(unbindBo);
        return Result.success();
    }
}
