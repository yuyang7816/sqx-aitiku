package com.sqx.app.base.template.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.template.domain.bo.SmsTemplateInfoBo;
import com.sqx.app.base.template.domain.bo.SmsTemplatePageBo;
import com.sqx.app.base.template.domain.bo.SmsTemplateSaveBo;
import com.sqx.app.base.template.domain.bo.SmsTemplateUpdateBo;
import com.sqx.app.base.template.domain.vo.SmsTemplateVo;
import com.sqx.app.base.template.service.SmsTemplateService;
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
 * 短信模板 前端控制器
 *
 * @author sqx
 * @since 2025-06-12 10:20:37
 */
@Tag(name = "", description = "")
@RestController
@RequestMapping("/template/smsTemplate")
public class SmsTemplateController {

    @Resource
    private SmsTemplateService smsTemplateService;

    /**
     * 保存
     */
    @Operation(summary = "保存")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated SmsTemplateSaveBo smsTemplateSaveBo) {
        return smsTemplateService.saveInfo(smsTemplateSaveBo);
    }

    /**
     * 列表
     */
    @Operation(summary = "列表")
    @ApiResponse(content = @Content(schema = @Schema(implementation = SmsTemplateVo.class)))
    @PostMapping("page")
    public Result<IPage<SmsTemplateVo>> page(@RequestBody @Validated SmsTemplatePageBo smsTemplatePageBo) {
        return smsTemplateService.pageInfo(smsTemplatePageBo);
    }

    /**
     * 信息
     */
    @Operation(summary = "信息")
    @ApiResponse(content = @Content(schema = @Schema(implementation = SmsTemplateVo.class)))
    @PostMapping("info")
    public Result<SmsTemplateVo> info(@RequestBody @Validated SmsTemplateInfoBo smsTemplateInfoBo) {
        return smsTemplateService.info(smsTemplateInfoBo);
    }

    /**
     * 更新
     */
    @Operation(summary = "更新")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated SmsTemplateUpdateBo smsTemplateUpdateBo) {
        return smsTemplateService.updateInfo(smsTemplateUpdateBo);
    }

    /**
     * 删除
     */
    @Operation(summary = "删除")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated SmsTemplateInfoBo smsTemplateInfoBo) {
        return smsTemplateService.delete(smsTemplateInfoBo);
    }
}
