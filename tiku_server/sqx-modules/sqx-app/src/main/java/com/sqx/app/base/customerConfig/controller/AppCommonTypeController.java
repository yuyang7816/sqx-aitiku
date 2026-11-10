package com.sqx.app.base.customerConfig.controller;

import com.sqx.app.base.appConfig.domain.vo.SysConfigVO;
import com.sqx.app.base.customerConfig.service.CustomerConfigService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * legacy 通用配置项（uniapp App.vue 注释中的 /app/common/type/{id}）
 */
@Tag(name = "用户端-通用配置项", description = "兼容 legacy common/type 拉取单项配置，无需登录")
@RestController
@RequestMapping("app/common")
public class AppCommonTypeController {

    @Resource
    private CustomerConfigService customerConfigService;

    @Operation(summary = "按 typeId 获取配置值", description = "客服相关：274=企业微信链接，275=企业微信AppId，328=客服类型，329=客服电话")
    @GetMapping("type/{id}")
    public Result<SysConfigVO> getByTypeId(@PathVariable("id") Long id) {
        return Result.success(customerConfigService.getByLegacyTypeId(id));
    }
}
