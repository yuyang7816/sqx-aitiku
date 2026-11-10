package com.sqx.app.vip.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.vip.pay.db.service.IVipOrderService;
import com.sqx.app.vip.pay.domain.bo.AdminVipOrderInfoBo;
import com.sqx.app.vip.pay.domain.bo.AdminVipOrderPageBo;
import com.sqx.app.vip.pay.domain.entity.VipOrder;
import com.sqx.app.vip.pay.domain.vo.AdminVipOrderVo;
import com.sqx.app.vip.pay.service.AdminVipOrderService;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AdminVipOrderServiceImpl implements AdminVipOrderService {

    @Resource
    private IVipOrderService iVipOrderService;
    @Resource
    private IUserService iUserService;

    @Override
    public IPage<AdminVipOrderVo> pageInfo(AdminVipOrderPageBo pageBo) {
        Page<VipOrder> page = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        LambdaQueryWrapper<VipOrder> wrapper = new LambdaQueryWrapper<VipOrder>()
                .eq(Objects.nonNull(pageBo.getUserId()), VipOrder::getUserId, pageBo.getUserId())
                .like(StringUtils.isNotBlank(pageBo.getOrderNo()), VipOrder::getOrderNo, pageBo.getOrderNo())
                .eq(Objects.nonNull(pageBo.getPayStatus()), VipOrder::getPayStatus, pageBo.getPayStatus())
                .orderByDesc(VipOrder::getCreateTime);
        IPage<VipOrder> orderPage = iVipOrderService.page(page, wrapper);
        if (orderPage.getRecords() == null || orderPage.getRecords().isEmpty()) {
            return PageUtil.copyPage(orderPage, Collections.emptyList());
        }
        Map<Long, UserEntity> userMap = loadUserMap(orderPage.getRecords());
        List<AdminVipOrderVo> voList = orderPage.getRecords().stream()
                .map(order -> toVo(order, userMap.get(order.getUserId())))
                .collect(Collectors.toList());
        return PageUtil.copyPage(orderPage, voList);
    }

    @Override
    public AdminVipOrderVo info(AdminVipOrderInfoBo infoBo) {
        VipOrder order = iVipOrderService.getById(infoBo.getId());
        if (order == null) {
            throw new BizException("订单不存在");
        }
        UserEntity user = iUserService.getById(order.getUserId());
        return toVo(order, user);
    }

    private Map<Long, UserEntity> loadUserMap(List<VipOrder> orders) {
        List<Long> userIds = orders.stream()
                .map(VipOrder::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return iUserService.listByIds(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getUserId, item -> item, (a, b) -> a));
    }

    private AdminVipOrderVo toVo(VipOrder order, UserEntity user) {
        AdminVipOrderVo vo = new AdminVipOrderVo();
        BeanUtils.copyProperties(order, vo);
        if (user != null) {
            vo.setUserName(user.getUserName());
            vo.setNickname(user.getNickname());
            vo.setPhone(user.getPhone());
            vo.setUserVipExpireAt(user.getVipExpireAt());
        }
        return vo;
    }
}
