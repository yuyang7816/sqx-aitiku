package com.sqx.app.base.template.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.template.db.service.ISmsTemplateService;
import com.sqx.app.base.template.domain.bo.SmsTemplateInfoBo;
import com.sqx.app.base.template.domain.bo.SmsTemplatePageBo;
import com.sqx.app.base.template.domain.bo.SmsTemplateSaveBo;
import com.sqx.app.base.template.domain.bo.SmsTemplateUpdateBo;
import com.sqx.app.base.template.domain.entity.SmsTemplate;
import com.sqx.app.base.template.domain.vo.SmsTemplateVo;
import com.sqx.app.base.template.service.SmsTemplateService;
import com.sqx.common.core.domain.Result;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.core.exception.SqxException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 短信模板 服务实现类
 *
 * @author sqx
 * @since 2025-06-12 10:20:37
 */
@Service
public class SmsTemplateServiceImpl implements SmsTemplateService {

    @Resource
    private ISmsTemplateService iSmsTemplateService;

    @Override
    public Result<Void> saveInfo(SmsTemplateSaveBo smsTemplateSaveBo) {
        SmsTemplate smsTemplate = new SmsTemplate();
        BeanUtils.copyProperties(smsTemplateSaveBo, smsTemplate);

        iSmsTemplateService.save(smsTemplate);
        return Result.success();
    }

    @Override
    public Result<IPage<SmsTemplateVo>> pageInfo(SmsTemplatePageBo smsTemplatePageBo) {
        Page<SmsTemplate> initPage = PageUtil.initPage(smsTemplatePageBo.getPage(), smsTemplatePageBo.getLimit());

        QueryWrapper<SmsTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StringUtils.isNotBlank(smsTemplatePageBo.getBizCode()), SmsTemplate::getBizCode, smsTemplatePageBo.getBizCode())
                .like(StringUtils.isNotBlank(smsTemplatePageBo.getContent()), SmsTemplate::getContent, smsTemplatePageBo.getContent())
                .orderByDesc(SmsTemplate::getCreateTime);

        IPage<SmsTemplate> iPage = iSmsTemplateService.page(initPage, queryWrapper);
        List<SmsTemplateVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(iPage.getRecords())) {
            for (SmsTemplate record : iPage.getRecords()) {
                SmsTemplateVo vo = new SmsTemplateVo();
                BeanUtils.copyProperties(record, vo);

                voList.add(vo);
            }
        }

        IPage<SmsTemplateVo> voIPage = PageUtil.copyPage(iPage, voList);
        return Result.success(voIPage);
    }

    @Override
    public Result<SmsTemplateVo> info(SmsTemplateInfoBo smsTemplateInfoBo) {
        SmsTemplate smsTemplate = iSmsTemplateService.getById(smsTemplateInfoBo.getId());
        if (Objects.isNull(smsTemplate)) {
            throw new SqxException("信息不存在");
        }

        SmsTemplateVo vo = new SmsTemplateVo();
        BeanUtils.copyProperties(smsTemplate, vo);
        return Result.success(vo);
    }

    @Override
    public Result<Void> updateInfo(SmsTemplateUpdateBo smsTemplateUpdateBo) {
        SmsTemplate smsTemplate = new SmsTemplate();
        BeanUtils.copyProperties(smsTemplateUpdateBo, smsTemplate);

        iSmsTemplateService.updateById(smsTemplate);
        return Result.success();
    }

    @Override
    public Result<Void> delete(SmsTemplateInfoBo smsTemplateInfoBo) {
        iSmsTemplateService.removeById(smsTemplateInfoBo.getId());
        return Result.success();
    }

    @Override
    public String getContentByBizCode(String bizCode) {
        QueryWrapper<SmsTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SmsTemplate::getBizCode, bizCode);
        SmsTemplate smsTemplate = iSmsTemplateService.getOne(queryWrapper);
        if (Objects.isNull(smsTemplate)) {
            throw new BizException(AppError.SMS_TEMPLATE_NOT_FOUND);
        }

        return smsTemplate.getContent();
    }
}
