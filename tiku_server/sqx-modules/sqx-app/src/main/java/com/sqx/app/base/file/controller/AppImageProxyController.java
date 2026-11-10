package com.sqx.app.base.file.controller;

import com.sqx.app.base.file.service.AppImageProxyService;
import com.sqx.common.core.exception.BizException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户端图片代理（海报 canvas 等同源拉取 OSS/CDN 图片）
 */
@Tag(name = "用户端-图片代理", description = "代理 OSS/CDN 图片，供海报 canvas 等同源加载")
@RestController
@RequestMapping("/app/common")
public class AppImageProxyController {

    @Resource
    private AppImageProxyService appImageProxyService;

    @Operation(summary = "代理远程图片", description = "仅允许已配置的 CDN/域名，供邀请海报等 canvas 绘制")
    @GetMapping("imageProxy")
    public void imageProxy(@RequestParam("url") String url, HttpServletResponse response) {
        try {
            appImageProxyService.proxyImage(url, response);
        } catch (BizException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
        }
    }
}
