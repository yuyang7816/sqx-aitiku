package com.sqx.app.vip.rights.controller;

import com.sqx.app.vip.rights.domain.vo.AppVipRightsCompareItemVO;
import com.sqx.app.vip.rights.domain.vo.VipRightsVo;
import com.sqx.app.vip.rights.service.VipRightsService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 会员特权 前端控制器
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-07 16:56:11
 */
@Tag(name = "用户端-会员特权", description = "用户端-会员特权")
@RestController
@RequestMapping("/app/vipRights")
public class AppVipRightsController {

    @Resource
    private VipRightsService vipRightsService;

    /**
     * 权益对比表（AI/OCR/视频/报告/去广告）
     */
    @Operation(summary = "权益对比表", description = "返回免费版与会员版权益对比项，AI/OCR 免费次数与 sys_config 同步")
    @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = AppVipRightsCompareItemVO.class))))
    @GetMapping
    public Result<List<AppVipRightsCompareItemVO>> compareList() {
        return Result.success(vipRightsService.listCompareItems());
    }

    /**
     * 特权图标列表（支付成功页等）
     */
    @Operation(summary = "特权图标列表")
    @ApiResponse(content = @Content(schema = @Schema(implementation = VipRightsVo.class)))
    @PostMapping("list")
    public Result<List<VipRightsVo>> list() {
        List<VipRightsVo> voList = vipRightsService.list();
        return Result.success(voList);
    }
}
