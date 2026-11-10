package com.sqx.app.vip.plan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sqx.app.vip.plan.db.service.IVipPlanService;
import com.sqx.app.vip.plan.domain.entity.VipPlan;
import com.sqx.app.vip.plan.domain.vo.AppVipPlanItemVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GET /app/vipPlan 启用套餐列表单元测试
 */
@ExtendWith(MockitoExtension.class)
class VipPlanServiceImplListAppPlansTest {

    @Mock
    private IVipPlanService iVipPlanService;

    @InjectMocks
    private VipPlanServiceImpl vipPlanService;

    @Test
    void listAppPlans_shouldReturnEnabledPlansWithNamePriceDaysOrderedBySort() {
        when(iVipPlanService.list(any(QueryWrapper.class))).thenReturn(Arrays.asList(
                buildPlan("月卡", 1, new BigDecimal("29.90"), 1, 1),
                buildPlan("季卡", 3, new BigDecimal("79.90"), 2, 1)
        ));

        List<AppVipPlanItemVO> result = vipPlanService.listAppPlans();

        assertEquals(2, result.size());
        assertEquals("月卡", result.get(0).getName());
        assertEquals(new BigDecimal("29.90"), result.get(0).getPrice());
        assertEquals(31, result.get(0).getDays());
        assertEquals("季卡", result.get(1).getName());
        assertEquals(new BigDecimal("79.90"), result.get(1).getPrice());
        assertEquals(90, result.get(1).getDays());

        ArgumentCaptor<QueryWrapper> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(iVipPlanService).list(captor.capture());
        assertNotNull(captor.getValue());
    }

    @Test
    void listAppPlans_whenNoRecords_shouldReturnEmptyList() {
        when(iVipPlanService.list(any(QueryWrapper.class))).thenReturn(Collections.emptyList());

        List<AppVipPlanItemVO> result = vipPlanService.listAppPlans();

        assertTrue(result.isEmpty());
    }

    @Test
    void resolveDays_shouldMapMonthToDays() {
        assertEquals(31, VipPlanServiceImpl.resolveDays(1));
        assertEquals(90, VipPlanServiceImpl.resolveDays(3));
        assertEquals(360, VipPlanServiceImpl.resolveDays(12));
        assertEquals(0, VipPlanServiceImpl.resolveDays(null));
        assertEquals(0, VipPlanServiceImpl.resolveDays(0));
    }

    private VipPlan buildPlan(String name, Integer month, BigDecimal amount, Integer sort, Integer status) {
        VipPlan plan = new VipPlan();
        plan.setName(name);
        plan.setMonth(month);
        plan.setAmount(amount);
        plan.setSort(sort);
        plan.setStatus(status);
        return plan;
    }
}
