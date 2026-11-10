package com.sqx.app.base.question.chapter.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.constants.BaseConstant;
import com.sqx.app.base.question.chapter.domain.bo.*;
import com.sqx.app.base.question.chapter.domain.vo.QuestionChapterVo;
import com.sqx.app.base.question.chapter.service.QuestionChapterAdminService;
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
 * 管理端题库-章
 */
@Tag(name = "管理端-题库章", description = "管理端维护 tb_question_chapter，供 GET /app/question/chapter/tree 读取")
@RestController
@RequestMapping(BaseConstant.BASE_ADMIN_PREFIX + "/question/chapter")
public class AdminQuestionChapterController {

    @Resource
    private QuestionChapterAdminService questionChapterAdminService;

    @Operation(summary = "新增章")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated QuestionChapterSaveBo saveBo) {
        questionChapterAdminService.saveInfo(saveBo);
        return Result.success();
    }

    @Operation(summary = "章分页列表")
    @ApiResponse(content = @Content(schema = @Schema(implementation = QuestionChapterVo.class)))
    @PostMapping("page")
    public Result<IPage<QuestionChapterVo>> page(@RequestBody @Validated QuestionChapterPageBo pageBo) {
        return Result.success(questionChapterAdminService.pageInfo(pageBo));
    }

    @Operation(summary = "章详情")
    @ApiResponse(content = @Content(schema = @Schema(implementation = QuestionChapterVo.class)))
    @PostMapping("info")
    public Result<QuestionChapterVo> info(@RequestBody @Validated QuestionChapterInfoBo infoBo) {
        return Result.success(questionChapterAdminService.info(infoBo));
    }

    @Operation(summary = "更新章")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated QuestionChapterUpdateBo updateBo) {
        questionChapterAdminService.updateInfo(updateBo);
        return Result.success();
    }

    @Operation(summary = "删除章")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated QuestionChapterInfoBo infoBo) {
        questionChapterAdminService.delete(infoBo);
        return Result.success();
    }

    @Operation(summary = "修改章状态")
    @PostMapping("updateState")
    public Result<Void> updateState(@RequestBody @Validated QuestionChapterUpdateStateBo updateStateBo) {
        questionChapterAdminService.updateState(updateStateBo);
        return Result.success();
    }
}
