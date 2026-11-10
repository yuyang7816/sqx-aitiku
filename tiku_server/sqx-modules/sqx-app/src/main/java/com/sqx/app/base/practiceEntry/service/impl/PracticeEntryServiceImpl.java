package com.sqx.app.base.practiceEntry.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.practiceEntry.domain.bo.*;
import com.sqx.app.base.practiceEntry.domain.vo.AppPracticeEntryItemVO;
import com.sqx.app.base.practiceEntry.domain.vo.PracticeEntryVo;
import com.sqx.app.base.practiceEntry.service.PracticeEntryService;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import com.sqx.system.db.entity.PracticeEntry;
import com.sqx.system.db.service.IPracticeEntryService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class PracticeEntryServiceImpl implements PracticeEntryService {

    private static final int STATE_ENABLED = 1;

    @Resource
    private IPracticeEntryService iPracticeEntryService;

    @Override
    public List<AppPracticeEntryItemVO> listAppEntries() {
        QueryWrapper<PracticeEntry> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(PracticeEntry::getState, STATE_ENABLED)
                .orderByAsc(PracticeEntry::getSortOrder)
                .orderByAsc(PracticeEntry::getId);

        List<PracticeEntry> records = iPracticeEntryService.list(queryWrapper);
        if (CollectionUtils.isEmpty(records)) {
            return new ArrayList<>();
        }

        List<AppPracticeEntryItemVO> voList = new ArrayList<>(records.size());
        for (PracticeEntry record : records) {
            AppPracticeEntryItemVO vo = new AppPracticeEntryItemVO();
            vo.setTitle(record.getTitle());
            vo.setSub(record.getSubtitle());
            vo.setIcon(record.getIcon());
            vo.setIconColor(record.getIconColor());
            vo.setBg(record.getBgStyle());
            vo.setUrl(record.getUrl());
            vo.setEntryType(StringUtils.trimToNull(record.getEntryType()));
            vo.setSort(record.getSortOrder());
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public void saveInfo(PracticeEntrySaveBo saveBo) {
        PracticeEntry entity = new PracticeEntry();
        BeanUtils.copyProperties(saveBo, entity);
        entity.setState(STATE_ENABLED);
        iPracticeEntryService.save(entity);
    }

    @Override
    public IPage<PracticeEntryVo> pageInfo(PracticeEntryPageBo pageBo) {
        Page<PracticeEntry> initPage = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        QueryWrapper<PracticeEntry> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StringUtils.isNotBlank(pageBo.getTitle()), PracticeEntry::getTitle, pageBo.getTitle())
                .eq(Objects.nonNull(pageBo.getState()), PracticeEntry::getState, pageBo.getState())
                .orderByAsc(PracticeEntry::getSortOrder)
                .orderByDesc(PracticeEntry::getCreateTime);

        IPage<PracticeEntry> iPage = iPracticeEntryService.page(initPage, queryWrapper);
        List<PracticeEntryVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(iPage.getRecords())) {
            for (PracticeEntry record : iPage.getRecords()) {
                PracticeEntryVo vo = new PracticeEntryVo();
                BeanUtils.copyProperties(record, vo);
                voList.add(vo);
            }
        }
        return PageUtil.copyPage(iPage, voList);
    }

    @Override
    public PracticeEntryVo info(PracticeEntryInfoBo infoBo) {
        PracticeEntry entity = iPracticeEntryService.getById(infoBo.getId());
        if (Objects.isNull(entity)) {
            throw new BizException(AppError.INFO_NOT_FOUND);
        }
        PracticeEntryVo vo = new PracticeEntryVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public void updateInfo(PracticeEntryUpdateBo updateBo) {
        PracticeEntry entity = new PracticeEntry();
        BeanUtils.copyProperties(updateBo, entity);
        iPracticeEntryService.updateById(entity);
    }

    @Override
    public void delete(PracticeEntryInfoBo infoBo) {
        iPracticeEntryService.removeById(infoBo.getId());
    }

    @Override
    public void updateState(PracticeEntryUpdateStateBo updateStateBo) {
        LambdaUpdateWrapper<PracticeEntry> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PracticeEntry::getId, updateStateBo.getId())
                .set(PracticeEntry::getState, updateStateBo.getState());
        iPracticeEntryService.update(new PracticeEntry(), updateWrapper);
    }
}
