package com.sqx.app.vip.pay.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.vip.pay.domain.bo.AdminVipOrderInfoBo;
import com.sqx.app.vip.pay.domain.bo.AdminVipOrderPageBo;
import com.sqx.app.vip.pay.domain.vo.AdminVipOrderVo;
import com.sqx.app.vip.pay.service.AdminVipOrderService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Tag(name = "管理端-会员订单", description = "管理端-会员订单")
@RestController
@RequestMapping("/admin/vipOrder")
public class AdminVipOrderController {

    @Resource
    private AdminVipOrderService adminVipOrderService;

    @Operation(summary = "订单列表")
    @PostMapping("page")
    public Result<IPage<AdminVipOrderVo>> page(@RequestBody @Validated AdminVipOrderPageBo pageBo) {
        return Result.success(adminVipOrderService.pageInfo(pageBo));
    }

    @Operation(summary = "订单详情")
    @PostMapping("info")
    public Result<AdminVipOrderVo> info(@RequestBody @Validated AdminVipOrderInfoBo infoBo) {
        return Result.success(adminVipOrderService.info(infoBo));
    }
}
