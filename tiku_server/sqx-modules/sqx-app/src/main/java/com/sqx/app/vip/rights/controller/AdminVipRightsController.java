package com.sqx.app.vip.rights.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.vip.rights.domain.bo.VipRightsInfoBo;
import com.sqx.app.vip.rights.domain.bo.VipRightsPageBo;
import com.sqx.app.vip.rights.domain.bo.VipRightsSaveBo;
import com.sqx.app.vip.rights.domain.bo.VipRightsUpdateBo;
import com.sqx.app.vip.rights.domain.vo.VipRightsVo;
import com.sqx.app.vip.rights.service.VipRightsService;
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
 * 会员特权 前端控制器
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-07 16:56:11
 */
@Tag(name = "管理端-会员特权", description = "管理端-会员特权")
@RestController
@RequestMapping("/admin/vipRights")
public class AdminVipRightsController {

    @Resource
    private VipRightsService vipRightsService;

    /**
     * 保存
     */
    @Operation(summary = "保存")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated VipRightsSaveBo vipRightsSaveBo) {
        vipRightsService.saveInfo(vipRightsSaveBo);
        return Result.success();
    }

    /**
     * 列表
     */
    @Operation(summary = "列表")
    @ApiResponse(content = @Content(schema = @Schema(implementation = VipRightsVo.class)))
    @PostMapping("page")
    public Result<IPage<VipRightsVo>> page(@RequestBody @Validated VipRightsPageBo vipRightsPageBo) {
        IPage<VipRightsVo> voIPage = vipRightsService.pageInfo(vipRightsPageBo);
        return Result.success(voIPage);
    }

    /**
     * 信息
     */
    @Operation(summary = "信息")
    @ApiResponse(content = @Content(schema = @Schema(implementation = VipRightsVo.class)))
    @PostMapping("info")
    public Result<VipRightsVo> info(@RequestBody @Validated VipRightsInfoBo vipRightsInfoBo) {
        VipRightsVo vo = vipRightsService.info(vipRightsInfoBo);
        return Result.success(vo);
    }

    /**
     * 更新
     */
    @Operation(summary = "更新")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated VipRightsUpdateBo vipRightsUpdateBo) {
        vipRightsService.updateInfo(vipRightsUpdateBo);
        return Result.success();
    }

    /**
     * 删除
     */
    @Operation(summary = "删除")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated VipRightsInfoBo vipRightsInfoBo) {
        vipRightsService.delete(vipRightsInfoBo);
        return Result.success();
    }
}
