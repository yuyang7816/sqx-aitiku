package com.sqx.app.base.question.chapter.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.constants.BaseConstant;
import com.sqx.app.base.question.chapter.domain.bo.*;
import com.sqx.app.base.question.chapter.domain.vo.QuestionSectionVo;
import com.sqx.app.base.question.chapter.service.QuestionSectionAdminService;
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
 * 管理端题库-节
 */
@Tag(name = "管理端-题库节", description = "管理端维护 tb_question_section")
@RestController
@RequestMapping(BaseConstant.BASE_ADMIN_PREFIX + "/question/section")
public class AdminQuestionSectionController {

    @Resource
    private QuestionSectionAdminService questionSectionAdminService;

    @Operation(summary = "新增节")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated QuestionSectionSaveBo saveBo) {
        questionSectionAdminService.saveInfo(saveBo);
        return Result.success();
    }

    @Operation(summary = "节分页列表")
    @ApiResponse(content = @Content(schema = @Schema(implementation = QuestionSectionVo.class)))
    @PostMapping("page")
    public Result<IPage<QuestionSectionVo>> page(@RequestBody @Validated QuestionSectionPageBo pageBo) {
        return Result.success(questionSectionAdminService.pageInfo(pageBo));
    }

    @Operation(summary = "节详情")
    @ApiResponse(content = @Content(schema = @Schema(implementation = QuestionSectionVo.class)))
    @PostMapping("info")
    public Result<QuestionSectionVo> info(@RequestBody @Validated QuestionSectionInfoBo infoBo) {
        return Result.success(questionSectionAdminService.info(infoBo));
    }

    @Operation(summary = "更新节")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated QuestionSectionUpdateBo updateBo) {
        questionSectionAdminService.updateInfo(updateBo);
        return Result.success();
    }

    @Operation(summary = "删除节")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated QuestionSectionInfoBo infoBo) {
        questionSectionAdminService.delete(infoBo);
        return Result.success();
    }

    @Operation(summary = "修改节状态")
    @PostMapping("updateState")
    public Result<Void> updateState(@RequestBody @Validated QuestionSectionUpdateStateBo updateStateBo) {
        questionSectionAdminService.updateState(updateStateBo);
        return Result.success();
    }
}
