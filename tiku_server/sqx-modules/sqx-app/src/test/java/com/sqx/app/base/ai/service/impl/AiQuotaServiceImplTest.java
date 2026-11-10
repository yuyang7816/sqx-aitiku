package com.sqx.app.base.ai.service.impl;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.sqx.app.base.ai.domain.vo.AppAiQuotaVO;
import com.sqx.app.base.ai.enums.AiError;
import com.sqx.app.base.ai.support.AiConfigReader;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.vip.expire.service.VipExpireService;
import com.sqx.app.vip.rules.support.MemberRulesSupport;
import com.sqx.common.core.exception.BizException;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AI 次数配额扣减单元测试
 */
@ExtendWith(MockitoExtension.class)
class AiQuotaServiceImplTest {

    @Mock
    private IUserService iUserService;

    @Mock
    private AiConfigReader aiConfigReader;

    @Mock
    private VipExpireService vipExpireService;

    @Spy
    private MemberRulesSupport memberRulesSupport = new MemberRulesSupport();

    @InjectMocks
    private AiQuotaServiceImpl aiQuotaService;

    @BeforeEach
    void initMybatisPlusEntityCache() {
        Configuration configuration = new Configuration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "test");
        TableInfoHelper.initTableInfo(assistant, UserEntity.class);
    }

    @Test
    void consumeChatQuota_whenUserNotFound_shouldThrow() {
        when(vipExpireService.refreshIfExpired(99L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class, () -> aiQuotaService.consumeChatQuota(99L));
        assertEquals(AppError.USER_NOT_FOUND.getCode(), ex.getErrorCode().getCode());
    }

    @Test
    void consumeChatQuota_whenVip_shouldNotDeduct() {
        when(aiConfigReader.getDailyLimit()).thenReturn(5);
        UserEntity user = buildUser(1L, 1, 5, LocalDate.now());
        when(vipExpireService.refreshIfExpired(1L)).thenReturn(user);

        AppAiQuotaVO quota = aiQuotaService.consumeChatQuota(1L);

        assertEquals(5, quota.getAiDailyLimit());
        assertEquals(5, quota.getAiUsedToday());
        assertTrue(quota.getAiUnlimited());
        verify(iUserService, never()).updateById(any());
    }

    @Test
    void consumeChatQuota_whenNewDay_shouldResetAndDeduct() {
        when(aiConfigReader.getDailyLimit()).thenReturn(5);
        UserEntity user = buildUser(2L, 0, 5, LocalDate.now().minusDays(1));
        when(vipExpireService.refreshIfExpired(2L)).thenReturn(user);
        when(iUserService.updateById(any(UserEntity.class))).thenReturn(true);

        AppAiQuotaVO quota = aiQuotaService.consumeChatQuota(2L);

        assertEquals(1, quota.getAiUsedToday());
        assertFalse(quota.getAiUnlimited());

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(iUserService).updateById(captor.capture());
        assertEquals(1, captor.getValue().getAiUsedToday());
        assertEquals(LocalDate.now(), captor.getValue().getAiQuotaDate());
    }

    @Test
    void consumeChatQuota_whenUnderLimit_shouldIncrement() {
        when(aiConfigReader.getDailyLimit()).thenReturn(5);
        UserEntity user = buildUser(3L, 0, 2, LocalDate.now());
        when(vipExpireService.refreshIfExpired(3L)).thenReturn(user);
        when(iUserService.updateById(any(UserEntity.class))).thenReturn(true);

        AppAiQuotaVO quota = aiQuotaService.consumeChatQuota(3L);

        assertEquals(3, quota.getAiUsedToday());
        assertFalse(quota.getAiUnlimited());
    }

    @Test
    void consumeChatQuota_whenExhausted_shouldThrowQuotaExceeded() {
        when(aiConfigReader.getDailyLimit()).thenReturn(5);
        UserEntity user = buildUser(4L, 0, 5, LocalDate.now());
        when(vipExpireService.refreshIfExpired(4L)).thenReturn(user);

        BizException ex = assertThrows(BizException.class, () -> aiQuotaService.consumeChatQuota(4L));
        assertEquals(AiError.QUOTA_EXCEEDED.getCode(), ex.getErrorCode().getCode());
        verify(iUserService, never()).updateById(any());
    }

    private UserEntity buildUser(Long userId, int isVip, int usedToday, LocalDate quotaDate) {
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        user.setIsVip(isVip);
        user.setAiUsedToday(usedToday);
        user.setAiQuotaDate(quotaDate);
        return user;
    }
}
