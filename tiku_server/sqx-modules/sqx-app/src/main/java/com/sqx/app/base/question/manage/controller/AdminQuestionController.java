package com.sqx.app.base.question.manage.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.constants.BaseConstant;
import com.sqx.app.base.question.manage.domain.bo.*;
import com.sqx.app.base.question.manage.domain.vo.QuestionBindOptionVo;
import com.sqx.app.base.question.manage.domain.vo.QuestionImportResultVo;
import com.sqx.app.base.question.manage.domain.vo.QuestionVo;
import com.sqx.app.base.question.manage.service.QuestionAdminService;
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
 * 管理端题库-题目
 */
@Tag(name = "管理端-题目", description = "管理端维护 tb_question，供 GET /app/question/list 读取")
@RestController
@RequestMapping(BaseConstant.BASE_ADMIN_PREFIX + "/question")
public class AdminQuestionController {

    @Resource
    private QuestionAdminService questionAdminService;

    @Operation(summary = "新增题目")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated QuestionSaveBo saveBo) {
        questionAdminService.saveInfo(saveBo);
        return Result.success();
    }

    @Operation(summary = "题目分页列表")
    @ApiResponse(content = @Content(schema = @Schema(implementation = QuestionVo.class)))
    @PostMapping({"page", "list"})
    public Result<IPage<QuestionVo>> page(@RequestBody @Validated QuestionPageBo pageBo) {
        return Result.success(questionAdminService.pageInfo(pageBo));
    }

    @Operation(summary = "题目详情")
    @ApiResponse(content = @Content(schema = @Schema(implementation = QuestionVo.class)))
    @PostMapping("info")
    public Result<QuestionVo> info(@RequestBody @Validated QuestionInfoBo infoBo) {
        return Result.success(questionAdminService.info(infoBo));
    }

    @Operation(summary = "更新题目")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated QuestionUpdateBo updateBo) {
        questionAdminService.updateInfo(updateBo);
        return Result.success();
    }

    @Operation(summary = "删除题目")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated QuestionInfoBo infoBo) {
        questionAdminService.delete(infoBo);
        return Result.success();
    }

    @Operation(summary = "修改题目状态")
    @PostMapping("updateState")
    public Result<Void> updateState(@RequestBody @Validated QuestionUpdateStateBo updateStateBo) {
        questionAdminService.updateState(updateStateBo);
        return Result.success();
    }

    @Operation(summary = "批量导入题目（JSON）")
    @ApiResponse(content = @Content(schema = @Schema(implementation = QuestionImportResultVo.class)))
    @PostMapping("import")
    public Result<QuestionImportResultVo> importBatch(@RequestBody @Validated QuestionImportBo importBo) {
        return Result.success(questionAdminService.importBatch(importBo));
    }

    @Operation(summary = "知识点下拉（题目表单）")
    @PostMapping("knowledgeOptions")
    public Result<java.util.List<QuestionBindOptionVo>> knowledgeOptions(@RequestBody QuestionBindOptionQueryBo queryBo) {
        return Result.success(questionAdminService.listKnowledgeOptions(queryBo));
    }

    @Operation(summary = "题集下拉（题目表单）")
    @PostMapping("paperOptions")
    public Result<java.util.List<QuestionBindOptionVo>> paperOptions(@RequestBody QuestionBindOptionQueryBo queryBo) {
        return Result.success(questionAdminService.listPaperOptions(queryBo));
    }
}
