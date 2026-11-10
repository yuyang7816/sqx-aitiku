package com.sqx.app.vip.plan.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.vip.plan.domain.bo.VipPlanInfoBo;
import com.sqx.app.vip.plan.domain.bo.VipPlanPageBo;
import com.sqx.app.vip.plan.domain.bo.VipPlanSaveBo;
import com.sqx.app.vip.plan.domain.bo.VipPlanUpdateBo;
import com.sqx.app.vip.plan.domain.vo.VipPlanVo;
import com.sqx.app.vip.plan.service.VipPlanService;
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
 * 会员套餐	 前端控制器
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-07 16:51:13
 */
@Tag(name = "管理端-会员套餐	", description = "管理端-会员套餐	")
@RestController
@RequestMapping("/admin/vipPlan")
public class AdminVipPlanController {

    @Resource
    private VipPlanService vipPlanService;

    /**
     * 保存
     */
    @Operation(summary = "保存")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated VipPlanSaveBo vipPlanSaveBo) {
        vipPlanService.saveInfo(vipPlanSaveBo);
        return Result.success();
    }

    /**
     * 列表
     */
    @Operation(summary = "列表")
    @ApiResponse(content = @Content(schema = @Schema(implementation = VipPlanVo.class)))
    @PostMapping("page")
    public Result<IPage<VipPlanVo>> page(@RequestBody @Validated VipPlanPageBo vipPlanPageBo) {
        IPage<VipPlanVo> voIPage = vipPlanService.pageInfo(vipPlanPageBo);
        return Result.success(voIPage);
    }

    /**
     * 信息
     */
    @Operation(summary = "信息")
    @ApiResponse(content = @Content(schema = @Schema(implementation = VipPlanVo.class)))
    @PostMapping("info")
    public Result<VipPlanVo> info(@RequestBody @Validated VipPlanInfoBo vipPlanInfoBo) {
        VipPlanVo vo = vipPlanService.info(vipPlanInfoBo);
        return Result.success(vo);
    }

    /**
     * 更新
     */
    @Operation(summary = "更新")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated VipPlanUpdateBo vipPlanUpdateBo) {
        vipPlanService.updateInfo(vipPlanUpdateBo);
        return Result.success();
    }

    /**
     * 删除
     */
    @Operation(summary = "删除")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated VipPlanInfoBo vipPlanInfoBo) {
        vipPlanService.delete(vipPlanInfoBo);
        return Result.success();
    }
}
