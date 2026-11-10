package com.sqx.app.base.articleCms.controller;

import com.sqx.app.base.articleCms.domain.vo.ArticleCmsVO;
import com.sqx.app.base.articleCms.service.ArticleCmsService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 协议与政策 CMS（uniapp article 页一次拉取标题与富文本正文）
 */
@Tag(name = "用户端/协议与政策", description = "按 type 返回协议/隐私/关于我们等富文本内容，无需登录")
@RestController
@RequestMapping("/app/article")
public class AppArticleController {

    @Resource
    private ArticleCmsService articleCmsService;

    @Operation(summary = "按类型获取文章", description = "type 取值：agreement / privacy / about")
    @GetMapping("{type}")
    public Result<ArticleCmsVO> getByType(@PathVariable("type") String type) {
        return Result.success(articleCmsService.getByType(type));
    }
}