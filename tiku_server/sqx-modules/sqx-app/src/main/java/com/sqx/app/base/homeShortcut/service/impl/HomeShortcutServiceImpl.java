package com.sqx.app.base.homeShortcut.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.homeShortcut.domain.bo.*;
import com.sqx.app.base.homeShortcut.domain.vo.AppHomeShortcutItemVO;
import com.sqx.app.base.homeShortcut.domain.vo.HomeShortcutVo;
import com.sqx.app.base.homeShortcut.service.HomeShortcutService;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import com.sqx.system.db.entity.HomeShortcut;
import com.sqx.system.db.service.IHomeShortcutService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class HomeShortcutServiceImpl implements HomeShortcutService {

    private static final int STATE_ENABLED = 1;

    @Resource
    private IHomeShortcutService iHomeShortcutService;

    @Override
    public List<AppHomeShortcutItemVO> listAppShortcuts() {
        QueryWrapper<HomeShortcut> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(HomeShortcut::getState, STATE_ENABLED)
                .orderByAsc(HomeShortcut::getSortOrder)
                .orderByAsc(HomeShortcut::getId);

        List<HomeShortcut> records = iHomeShortcutService.list(queryWrapper);
        if (CollectionUtils.isEmpty(records)) {
            return new ArrayList<>();
        }

        List<AppHomeShortcutItemVO> voList = new ArrayList<>(records.size());
        for (HomeShortcut record : records) {
            AppHomeShortcutItemVO vo = new AppHomeShortcutItemVO();
            vo.setTitle(record.getTitle());
            vo.setIcon(record.getIcon());
            vo.setColor(record.getIconColor());
            vo.setBg(record.getBgStyle());
            vo.setUrl(record.getUrl());
            vo.setAction(StringUtils.trimToNull(record.getActionType()));
            vo.setSort(record.getSortOrder());
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public void saveInfo(HomeShortcutSaveBo saveBo) {
        HomeShortcut entity = new HomeShortcut();
        BeanUtils.copyProperties(saveBo, entity);
        entity.setState(STATE_ENABLED);
        iHomeShortcutService.save(entity);
    }

    @Override
    public IPage<HomeShortcutVo> pageInfo(HomeShortcutPageBo pageBo) {
        Page<HomeShortcut> initPage = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        QueryWrapper<HomeShortcut> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StringUtils.isNotBlank(pageBo.getTitle()), HomeShortcut::getTitle, pageBo.getTitle())
                .eq(Objects.nonNull(pageBo.getState()), HomeShortcut::getState, pageBo.getState())
                .orderByAsc(HomeShortcut::getSortOrder)
                .orderByDesc(HomeShortcut::getCreateTime);

        IPage<HomeShortcut> iPage = iHomeShortcutService.page(initPage, queryWrapper);
        List<HomeShortcutVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(iPage.getRecords())) {
            for (HomeShortcut record : iPage.getRecords()) {
                HomeShortcutVo vo = new HomeShortcutVo();
                BeanUtils.copyProperties(record, vo);
                voList.add(vo);
            }
        }
        return PageUtil.copyPage(iPage, voList);
    }

    @Override
    public HomeShortcutVo info(HomeShortcutInfoBo infoBo) {
        HomeShortcut entity = iHomeShortcutService.getById(infoBo.getId());
        if (Objects.isNull(entity)) {
            throw new BizException(AppError.INFO_NOT_FOUND);
        }
        HomeShortcutVo vo = new HomeShortcutVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public void updateInfo(HomeShortcutUpdateBo updateBo) {
        HomeShortcut entity = new HomeShortcut();
        BeanUtils.copyProperties(updateBo, entity);
        iHomeShortcutService.updateById(entity);
    }

    @Override
    public void delete(HomeShortcutInfoBo infoBo) {
        iHomeShortcutService.removeById(infoBo.getId());
    }

    @Override
    public void updateState(HomeShortcutUpdateStateBo updateStateBo) {
        LambdaUpdateWrapper<HomeShortcut> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(HomeShortcut::getId, updateStateBo.getId())
                .set(HomeShortcut::getState, updateStateBo.getState());
        iHomeShortcutService.update(new HomeShortcut(), updateWrapper);
    }
}
