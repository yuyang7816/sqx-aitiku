package com.sqx.app.base.subscribe.controller;

import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.subscribe.domain.bo.SubscribeRegisterBO;
import com.sqx.app.base.subscribe.domain.bo.SubscribeTestSendBO;
import com.sqx.app.base.subscribe.domain.vo.SubscribeRegisterVO;
import com.sqx.app.base.subscribe.domain.vo.SubscribeTemplateVO;
import com.sqx.app.base.subscribe.domain.vo.SubscribeTestSendVO;
import com.sqx.app.base.subscribe.service.SubscribePushService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户端微信订阅消息：模板配置与测试发送
 */
@Tag(name = "用户端-订阅消息", description = "微信订阅消息模板与测试推送")
@RestController
@RequestMapping("/app/subscribe")
@SecurityRequirement(name = "appToken")
public class AppSubscribeMessageController {

    @Resource
    private SubscribePushService subscribePushService;

    @Operation(summary = "订阅消息模板 ID", description = "返回月报/会员到期/错题提醒三类模板 ID，供 uniapp requestSubscribeMessage 授权")
    @GetMapping("templates")
    public Result<SubscribeTemplateVO> templates() {
        return Result.success(subscribePushService.getTemplates());
    }

    @Operation(summary = "发送测试订阅消息", description = "向当前登录用户发送指定场景测试消息，需已配置模板 ID 且账号有 maOpenId")
    @Login
    @PostMapping("test")
    public Result<SubscribeTestSendVO> test(@RequestAttribute("userId") Long userId,
                                            @Validated @RequestBody SubscribeTestSendBO body) {
        return Result.success(subscribePushService.sendTest(userId, body.getScene()));
    }

    @Operation(summary = "登记订阅消息偏好", description = "设置页开启学习提醒并授权后登记，供定时推送任务读取")
    @Login
    @PostMapping("register")
    public Result<SubscribeRegisterVO> register(@RequestAttribute("userId") Long userId,
                                                @Validated @RequestBody SubscribeRegisterBO body) {
        return Result.success(subscribePushService.registerPreferences(userId, body));
    }
}
