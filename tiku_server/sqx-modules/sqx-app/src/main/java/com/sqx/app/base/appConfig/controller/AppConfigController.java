package com.sqx.app.base.appConfig.controller;

import com.sqx.app.base.appConfig.domain.bo.SysConfigInfoBO;
import com.sqx.app.base.appConfig.domain.vo.SysConfigVO;
import com.sqx.app.base.appConfig.service.AppConfigService;
import com.sqx.app.base.appConfig.domain.vo.WechatPublicConfigVO;
import com.sqx.app.base.appConfig.service.WechatPublicConfigService;
import com.sqx.app.base.customerConfig.domain.vo.CustomerConfigVO;
import com.sqx.app.base.customerConfig.service.CustomerConfigService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author chengming
 * @since 2025/6/9
 */
@Tag(name = "用户端-系统配置")
@RestController
@RequestMapping("app/config")
public class AppConfigController {

    @Resource
    private AppConfigService appConfigService;

    @Resource
    private CustomerConfigService customerConfigService;

    @Resource
    private WechatPublicConfigService wechatPublicConfigService;

    @Operation(summary = "系统配置信息")
    @PostMapping("info")
    public Result<SysConfigVO> info(@RequestBody SysConfigInfoBO sysConfigInfoBO) {
        SysConfigVO vo = appConfigService.info(sysConfigInfoBO);
        return Result.success(vo);
    }

    @Operation(summary = "客服配置", description = "返回客服电话、在线客服链接、入口开关等结构化 JSON，无需登录")
    @GetMapping("customer")
    public Result<CustomerConfigVO> customer() {
        return Result.success(customerConfigService.getCustomerConfig());
    }

    @Operation(summary = "微信公开配置", description = "返回公众号/小程序 AppId（不含 secret），供 H5 公众号 OAuth 使用")
    @GetMapping("wechat")
    public Result<WechatPublicConfigVO> wechat() {
        return Result.success(wechatPublicConfigService.getPublicConfig());
    }
}
