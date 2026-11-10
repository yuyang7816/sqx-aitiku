package com.sqx.app.referral.withdraw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.app.referral.withdraw.constants.WithdrawChannelConstants;
import com.sqx.app.referral.withdraw.db.service.IWithdrawalAccountService;
import com.sqx.app.referral.withdraw.domain.bo.AppWithdrawalAccountBindBO;
import com.sqx.app.referral.withdraw.domain.entity.WithdrawalAccount;
import com.sqx.app.referral.withdraw.domain.vo.AppWithdrawalAccountBindResultVO;
import com.sqx.common.core.exception.BizException;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * POST /app/withdrawalAccount 实名绑定单元测试
 */
@ExtendWith(MockitoExtension.class)
class WithdrawalAccountServiceImplBindTest {

    @Mock
    private IWithdrawalAccountService iWithdrawalAccountService;

    @InjectMocks
    private WithdrawalAccountServiceImpl withdrawalAccountService;

    @BeforeEach
    void initEntityCache() {
        Configuration configuration = new Configuration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "test");
        TableInfoHelper.initTableInfo(assistant, WithdrawalAccount.class);
    }

    @Test
    void bindRealNameAccount_whenNew_shouldSaveVerifiedAccount() {
        when(iWithdrawalAccountService.getOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(iWithdrawalAccountService.save(any(WithdrawalAccount.class))).thenAnswer(invocation -> {
            WithdrawalAccount account = invocation.getArgument(0);
            account.setId(100L);
            account.setUpdateTime(java.time.LocalDateTime.of(2026, 8, 12, 12, 0, 0));
            return true;
        });

        AppWithdrawalAccountBindBO bindBO = buildBindBO();

        AppWithdrawalAccountBindResultVO result = withdrawalAccountService.bindRealNameAccount(300L, bindBO);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("张三", result.getRealName());
        assertEquals("1101**********1234", result.getIdCardNoMasked());
        assertEquals(1, result.getRealNameVerified());
        assertEquals("微信零钱", result.getTypeName());

        ArgumentCaptor<WithdrawalAccount> captor = ArgumentCaptor.forClass(WithdrawalAccount.class);
        verify(iWithdrawalAccountService).save(captor.capture());
        assertEquals("110101199001011234", captor.getValue().getIdCardNo());
        assertEquals(Integer.valueOf(1), captor.getValue().getRealNameVerified());
    }

    @Test
    void bindRealNameAccount_whenExists_shouldUpdateVerifiedAccount() {
        WithdrawalAccount existing = new WithdrawalAccount();
        existing.setId(200L);
        existing.setUserId(400L);
        existing.setType(WithdrawChannelConstants.ACCOUNT_TYPE_WECHAT);
        when(iWithdrawalAccountService.getOne(any(LambdaQueryWrapper.class))).thenReturn(existing);
        when(iWithdrawalAccountService.updateById(any(WithdrawalAccount.class))).thenReturn(true);

        AppWithdrawalAccountBindResultVO result = withdrawalAccountService.bindRealNameAccount(400L, buildBindBO());

        assertNotNull(result);
        assertEquals(200L, result.getId());
        verify(iWithdrawalAccountService).updateById(any(WithdrawalAccount.class));
        verify(iWithdrawalAccountService, never()).save(any(WithdrawalAccount.class));
    }

    @Test
    void bindRealNameAccount_whenInvalidType_shouldThrow() {
        AppWithdrawalAccountBindBO bindBO = buildBindBO();
        bindBO.setType(99);

        assertThrows(BizException.class, () -> withdrawalAccountService.bindRealNameAccount(500L, bindBO));
    }

    private AppWithdrawalAccountBindBO buildBindBO() {
        AppWithdrawalAccountBindBO bindBO = new AppWithdrawalAccountBindBO();
        bindBO.setRealName("张三");
        bindBO.setIdCardNo("110101199001011234");
        bindBO.setAccountName("张三");
        bindBO.setAccountNumber("wx_openid_demo");
        bindBO.setType(WithdrawChannelConstants.ACCOUNT_TYPE_WECHAT);
        return bindBO;
    }
}
