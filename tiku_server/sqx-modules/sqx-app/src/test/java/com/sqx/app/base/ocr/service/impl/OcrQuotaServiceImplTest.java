package com.sqx.app.base.ocr.service.impl;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.ocr.domain.vo.AppOcrQuotaVO;
import com.sqx.app.base.ocr.enums.OcrError;
import com.sqx.app.base.ocr.support.OcrConfigReader;
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
 * OCR 次数配额扣减单元测试
 */
@ExtendWith(MockitoExtension.class)
class OcrQuotaServiceImplTest {

    @Mock
    private IUserService iUserService;

    @Mock
    private OcrConfigReader ocrConfigReader;

    @Mock
    private VipExpireService vipExpireService;

    @Spy
    private MemberRulesSupport memberRulesSupport = new MemberRulesSupport();

    @InjectMocks
    private OcrQuotaServiceImpl ocrQuotaService;

    @BeforeEach
    void initMybatisPlusEntityCache() {
        Configuration configuration = new Configuration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "test");
        TableInfoHelper.initTableInfo(assistant, UserEntity.class);
    }

    @Test
    void consumeSearchQuota_whenUserNotFound_shouldThrow() {
        when(vipExpireService.refreshIfExpired(99L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class, () -> ocrQuotaService.consumeSearchQuota(99L));
        assertEquals(AppError.USER_NOT_FOUND.getCode(), ex.getErrorCode().getCode());
    }

    @Test
    void consumeSearchQuota_whenVip_shouldNotDeduct() {
        when(ocrConfigReader.getDailyLimit()).thenReturn(5);
        UserEntity user = buildUser(1L, 1, 5, LocalDate.now());
        when(vipExpireService.refreshIfExpired(1L)).thenReturn(user);

        AppOcrQuotaVO quota = ocrQuotaService.consumeSearchQuota(1L);

        assertEquals(5, quota.getOcrDailyLimit());
        assertEquals(5, quota.getOcrUsedToday());
        assertTrue(quota.getOcrUnlimited());
        verify(iUserService, never()).updateById(any());
    }

    @Test
    void consumeSearchQuota_whenNewDay_shouldResetAndDeduct() {
        when(ocrConfigReader.getDailyLimit()).thenReturn(5);
        UserEntity user = buildUser(2L, 0, 5, LocalDate.now().minusDays(1));
        when(vipExpireService.refreshIfExpired(2L)).thenReturn(user);
        when(iUserService.updateById(any(UserEntity.class))).thenReturn(true);

        AppOcrQuotaVO quota = ocrQuotaService.consumeSearchQuota(2L);

        assertEquals(1, quota.getOcrUsedToday());
        assertFalse(quota.getOcrUnlimited());

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(iUserService).updateById(captor.capture());
        assertEquals(1, captor.getValue().getOcrUsedToday());
        assertEquals(LocalDate.now(), captor.getValue().getOcrQuotaDate());
    }

    @Test
    void consumeSearchQuota_whenUnderLimit_shouldIncrement() {
        when(ocrConfigReader.getDailyLimit()).thenReturn(5);
        UserEntity user = buildUser(3L, 0, 2, LocalDate.now());
        when(vipExpireService.refreshIfExpired(3L)).thenReturn(user);
        when(iUserService.updateById(any(UserEntity.class))).thenReturn(true);

        AppOcrQuotaVO quota = ocrQuotaService.consumeSearchQuota(3L);

        assertEquals(3, quota.getOcrUsedToday());
        assertFalse(quota.getOcrUnlimited());
    }

    @Test
    void consumeSearchQuota_whenExhausted_shouldThrowQuotaExceeded() {
        when(ocrConfigReader.getDailyLimit()).thenReturn(5);
        UserEntity user = buildUser(4L, 0, 5, LocalDate.now());
        when(vipExpireService.refreshIfExpired(4L)).thenReturn(user);

        BizException ex = assertThrows(BizException.class, () -> ocrQuotaService.consumeSearchQuota(4L));
        assertEquals(OcrError.QUOTA_EXCEEDED.getCode(), ex.getErrorCode().getCode());
        verify(iUserService, never()).updateById(any());
    }

    private UserEntity buildUser(Long userId, int isVip, int usedToday, LocalDate quotaDate) {
        UserEntity user = new UserEntity();
        user.setUserId(userId);
        user.setIsVip(isVip);
        user.setOcrUsedToday(usedToday);
        user.setOcrQuotaDate(quotaDate);
        return user;
    }
}
