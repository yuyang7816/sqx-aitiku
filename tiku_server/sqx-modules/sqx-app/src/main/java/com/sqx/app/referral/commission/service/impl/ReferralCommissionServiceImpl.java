package com.sqx.app.referral.commission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.base.user.support.AdminUserQuerySupport;
import com.sqx.app.referral.commission.db.service.IReferralCommissionService;
import com.sqx.app.referral.commission.domain.bo.ReferralCommissionInfoBo;
import com.sqx.app.referral.commission.domain.bo.ReferralCommissionPageBo;
import com.sqx.app.referral.commission.domain.bo.ReferralCommissionSaveBo;
import com.sqx.app.referral.commission.domain.bo.ReferralCommissionUpdateBo;
import com.sqx.app.referral.commission.domain.entity.ReferralCommission;
import com.sqx.app.referral.commission.domain.vo.ReferralCommissionVo;
import com.sqx.app.referral.commission.service.ReferralCommissionService;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 佣金信息 服务实现类
 *
 * @author 西安省钱兄网络科技有限公司
 * @since 2025-08-04 14:44:40
 */
@Slf4j
@Service
public class ReferralCommissionServiceImpl implements ReferralCommissionService {

    @Resource
    private IReferralCommissionService iReferralCommissionService;

    @Resource
    private IUserService iUserService;

    @Override
    public void saveInfo(ReferralCommissionSaveBo referralCommissionSaveBo) {
        ReferralCommission referralCommission = new ReferralCommission();
        BeanUtils.copyProperties(referralCommissionSaveBo, referralCommission);

        iReferralCommissionService.save(referralCommission);
    }

    @Override
    public IPage<ReferralCommissionVo> pageInfo(ReferralCommissionPageBo referralCommissionPageBo) {
        Page<ReferralCommission> initPage = PageUtil.initPage(referralCommissionPageBo.getPage(), referralCommissionPageBo.getLimit());

        List<Long> filterUserIds = AdminUserQuerySupport.resolveFilterUserIds(
                iUserService,
                referralCommissionPageBo.getUserId(),
                referralCommissionPageBo.getUserName(),
                referralCommissionPageBo.getPhone()
        );
        if (filterUserIds != null && filterUserIds.isEmpty()) {
            return PageUtil.copyPage(initPage, Collections.emptyList());
        }

        QueryWrapper<ReferralCommission> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Objects.nonNull(referralCommissionPageBo.getUserId()), ReferralCommission::getUserId, referralCommissionPageBo.getUserId())
                .in(filterUserIds != null, ReferralCommission::getUserId, filterUserIds)
                .eq(Objects.nonNull(referralCommissionPageBo.getOrderId()), ReferralCommission::getOrderId, referralCommissionPageBo.getOrderId())
                .eq(Objects.nonNull(referralCommissionPageBo.getType()), ReferralCommission::getType, referralCommissionPageBo.getType())
                .eq(Objects.nonNull(referralCommissionPageBo.getDirection()), ReferralCommission::getDirection, referralCommissionPageBo.getDirection())
                .orderByDesc(ReferralCommission::getCreateTime);

        IPage<ReferralCommission> iPage = iReferralCommissionService.page(initPage, queryWrapper);
        List<ReferralCommissionVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(iPage.getRecords())) {
            Map<Long, UserEntity> userMap = AdminUserQuerySupport.loadUserMap(
                    iUserService,
                    iPage.getRecords().stream().map(ReferralCommission::getUserId).collect(Collectors.toList())
            );
            for (ReferralCommission record : iPage.getRecords()) {
                ReferralCommissionVo vo = new ReferralCommissionVo();
                BeanUtils.copyProperties(record, vo);
                AdminUserQuerySupport.fillUserDisplay(userMap.get(record.getUserId()), vo);
                voList.add(vo);
            }
        }

        return PageUtil.copyPage(iPage, voList);
    }

    @Override
    public ReferralCommissionVo info(ReferralCommissionInfoBo referralCommissionInfoBo) {
        ReferralCommission referralCommission = iReferralCommissionService.getById(referralCommissionInfoBo.getId());
        if (Objects.isNull(referralCommission)) {
            throw new BizException("信息不存在");
        }

        ReferralCommissionVo vo = new ReferralCommissionVo();
        BeanUtils.copyProperties(referralCommission, vo);
        AdminUserQuerySupport.fillUserDisplay(iUserService.getById(referralCommission.getUserId()), vo);
        return vo;
    }

    @Override
    public void updateInfo(ReferralCommissionUpdateBo referralCommissionUpdateBo) {
        ReferralCommission referralCommission = new ReferralCommission();
        BeanUtils.copyProperties(referralCommissionUpdateBo, referralCommission);

        iReferralCommissionService.updateById(referralCommission);
    }

    @Override
    public void delete(ReferralCommissionInfoBo referralCommissionInfoBo) {
        iReferralCommissionService.removeById(referralCommissionInfoBo.getId());
    }
}
