package com.sqx.app.vip.pay.controller;

import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.vip.pay.domain.bo.AppVipOrderQueryBO;
import com.sqx.app.vip.pay.domain.vo.AppVipOrderItemVO;
import com.sqx.app.vip.pay.service.AppPayService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户端会员订单列表（GET /app/vipOrder，需登录）
 */
@Tag(name = "用户端-会员订单", description = "当前用户历史会员支付订单，按业务订单号/套餐名/支付状态筛选")
@RestController
@RequestMapping("/app/vipOrder")
@SecurityRequirement(name = "appToken")
public class AppVipOrderController {

    @Resource
    private AppPayService appPayService;

    @Operation(summary = "会员订单列表", description = "返回当前登录用户订单，按创建时间倒序；筛选项为 orderNo/planName/payStatus")
    @ApiResponse(content = @Content(array = @ArraySchema(schema = @Schema(implementation = AppVipOrderItemVO.class))))
    @Login
    @GetMapping
    public Result<List<AppVipOrderItemVO>> list(@RequestAttribute("userId") Long userId,
                                                AppVipOrderQueryBO query) {
        return Result.success(appPayService.listMyOrders(userId, query));
    }
}
