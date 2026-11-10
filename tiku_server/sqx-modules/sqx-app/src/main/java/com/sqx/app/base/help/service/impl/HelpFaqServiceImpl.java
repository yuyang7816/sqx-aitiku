package com.sqx.app.base.help.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.help.db.service.IHelpFaqService;
import com.sqx.app.base.help.domain.bo.HelpFaqInfoBo;
import com.sqx.app.base.help.domain.bo.HelpFaqPageBo;
import com.sqx.app.base.help.domain.bo.HelpFaqSaveBo;
import com.sqx.app.base.help.domain.bo.HelpFaqUpdateBo;
import com.sqx.app.base.help.domain.entity.HelpFaq;
import com.sqx.app.base.help.domain.vo.AppHelpFaqItemVO;
import com.sqx.app.base.help.domain.vo.HelpFaqVo;
import com.sqx.app.base.help.service.HelpFaqService;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class HelpFaqServiceImpl implements HelpFaqService {

    private static final int STATE_ENABLED = 1;

    @Resource
    private IHelpFaqService iHelpFaqService;

    @Override
    public List<AppHelpFaqItemVO> listAppFaqs() {
        QueryWrapper<HelpFaq> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(HelpFaq::getState, STATE_ENABLED)
                .orderByAsc(HelpFaq::getSort)
                .orderByDesc(HelpFaq::getCreateTime);

        List<HelpFaq> records = iHelpFaqService.list(queryWrapper);
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyList();
        }

        List<AppHelpFaqItemVO> voList = new ArrayList<>(records.size());
        for (HelpFaq record : records) {
            if (StringUtils.isBlank(record.getQuestion()) || StringUtils.isBlank(record.getAnswer())) {
                continue;
            }
            AppHelpFaqItemVO vo = new AppHelpFaqItemVO();
            vo.setId(record.getId());
            vo.setQuestion(record.getQuestion().trim());
            vo.setAnswer(record.getAnswer().trim());
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public void saveInfo(HelpFaqSaveBo saveBo) {
        HelpFaq entity = new HelpFaq();
        entity.setQuestion(StringUtils.trimToEmpty(saveBo.getQuestion()));
        entity.setAnswer(StringUtils.trimToEmpty(saveBo.getAnswer()));
        entity.setSort(saveBo.getSort() != null ? saveBo.getSort() : 0);
        entity.setState(STATE_ENABLED);
        iHelpFaqService.save(entity);
    }

    @Override
    public IPage<HelpFaqVo> pageInfo(HelpFaqPageBo pageBo) {
        Page<HelpFaq> initPage = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());

        QueryWrapper<HelpFaq> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StringUtils.isNotBlank(pageBo.getQuestion()), HelpFaq::getQuestion, pageBo.getQuestion())
                .eq(Objects.nonNull(pageBo.getState()), HelpFaq::getState, pageBo.getState())
                .orderByAsc(HelpFaq::getSort)
                .orderByDesc(HelpFaq::getCreateTime);

        IPage<HelpFaq> iPage = iHelpFaqService.page(initPage, queryWrapper);
        List<HelpFaqVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(iPage.getRecords())) {
            for (HelpFaq record : iPage.getRecords()) {
                HelpFaqVo vo = new HelpFaqVo();
                BeanUtils.copyProperties(record, vo);
                voList.add(vo);
            }
        }
        return PageUtil.copyPage(iPage, voList);
    }

    @Override
    public HelpFaqVo info(HelpFaqInfoBo infoBo) {
        HelpFaq entity = iHelpFaqService.getById(infoBo.getId());
        if (entity == null) {
            throw new BizException(AppError.INFO_NOT_FOUND);
        }
        HelpFaqVo vo = new HelpFaqVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public void updateInfo(HelpFaqUpdateBo updateBo) {
        HelpFaq existing = iHelpFaqService.getById(updateBo.getId());
        if (existing == null) {
            throw new BizException(AppError.INFO_NOT_FOUND);
        }

        HelpFaq entity = new HelpFaq();
        entity.setId(updateBo.getId());
        if (StringUtils.isNotBlank(updateBo.getQuestion())) {
            entity.setQuestion(updateBo.getQuestion().trim());
        }
        if (StringUtils.isNotBlank(updateBo.getAnswer())) {
            entity.setAnswer(updateBo.getAnswer().trim());
        }
        if (updateBo.getSort() != null) {
            entity.setSort(updateBo.getSort());
        }
        if (updateBo.getState() != null) {
            entity.setState(updateBo.getState());
        }
        iHelpFaqService.updateById(entity);
    }

    @Override
    public void delete(HelpFaqInfoBo infoBo) {
        HelpFaq existing = iHelpFaqService.getById(infoBo.getId());
        if (existing == null) {
            throw new BizException(AppError.INFO_NOT_FOUND);
        }
        iHelpFaqService.removeById(infoBo.getId());
    }
}
