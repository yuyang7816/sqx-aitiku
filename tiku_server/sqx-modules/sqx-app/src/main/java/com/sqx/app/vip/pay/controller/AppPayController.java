package com.sqx.app.vip.pay.controller;

import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.vip.pay.domain.bo.AppPayCreateBO;
import com.sqx.app.vip.pay.domain.vo.AppPayCreateVO;
import com.sqx.app.vip.pay.service.AppPayService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户端会员支付（POST /app/pay，需登录）
 */
@Tag(name = "用户端-会员支付", description = "创建会员订单并返回微信 prepay 参数")
@RestController
@RequestMapping("/app/pay")
@SecurityRequirement(name = "appToken")
public class AppPayController {

    @Resource
    private AppPayService appPayService;

    @Operation(summary = "创建会员支付订单", description = "按套餐名称创建待支付订单，返回微信 JSAPI prepay 参数供 uni.requestPayment 调起")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AppPayCreateVO.class)))
    @Login
    @PostMapping
    public Result<AppPayCreateVO> create(@RequestAttribute("userId") Long userId,
                                         @RequestBody @Validated AppPayCreateBO create) {
        return Result.success(appPayService.createOrder(userId, create));
    }
}
