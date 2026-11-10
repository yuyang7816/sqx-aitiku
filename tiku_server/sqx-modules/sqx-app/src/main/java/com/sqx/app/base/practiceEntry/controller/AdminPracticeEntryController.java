package com.sqx.app.base.practiceEntry.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.constants.BaseConstant;
import com.sqx.app.base.practiceEntry.domain.bo.*;
import com.sqx.app.base.practiceEntry.domain.vo.PracticeEntryVo;
import com.sqx.app.base.practiceEntry.service.PracticeEntryService;
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

@Tag(name = "管理端-练习入口")
@RestController
@RequestMapping(BaseConstant.BASE_ADMIN_PREFIX + "/practiceEntry")
public class AdminPracticeEntryController {

    @Resource
    private PracticeEntryService practiceEntryService;

    @Operation(summary = "新增")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated PracticeEntrySaveBo saveBo) {
        practiceEntryService.saveInfo(saveBo);
        return Result.success();
    }

    @Operation(summary = "分页列表")
    @ApiResponse(content = @Content(schema = @Schema(implementation = PracticeEntryVo.class)))
    @PostMapping("page")
    public Result<IPage<PracticeEntryVo>> page(@RequestBody @Validated PracticeEntryPageBo pageBo) {
        return Result.success(practiceEntryService.pageInfo(pageBo));
    }

    @Operation(summary = "详情")
    @PostMapping("info")
    public Result<PracticeEntryVo> info(@RequestBody @Validated PracticeEntryInfoBo infoBo) {
        return Result.success(practiceEntryService.info(infoBo));
    }

    @Operation(summary = "更新")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated PracticeEntryUpdateBo updateBo) {
        practiceEntryService.updateInfo(updateBo);
        return Result.success();
    }

    @Operation(summary = "删除")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated PracticeEntryInfoBo infoBo) {
        practiceEntryService.delete(infoBo);
        return Result.success();
    }

    @Operation(summary = "修改启用状态")
    @PostMapping("updateState")
    public Result<Void> updateState(@RequestBody @Validated PracticeEntryUpdateStateBo updateStateBo) {
        practiceEntryService.updateState(updateStateBo);
        return Result.success();
    }
}
