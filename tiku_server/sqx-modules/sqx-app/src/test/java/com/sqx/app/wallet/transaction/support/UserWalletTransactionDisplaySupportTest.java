package com.sqx.app.wallet.transaction.support;

import com.sqx.app.wallet.transaction.constants.UserWalletTransactionConstant;
import com.sqx.app.wallet.transaction.domain.entity.UserWalletTransaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 收益流水展示字段转换单元测试
 */
class UserWalletTransactionDisplaySupportTest {

    @Test
    void formatRecordTime_shouldFormatAsMinutePrecision() {
        String time = UserWalletTransactionDisplaySupport.formatRecordTime(
                LocalDateTime.of(2026, 6, 12, 11, 25));
        assertEquals("2026-06-12 11:25", time);
    }

    @Test
    void formatAmount_shouldKeepTwoDecimals() {
        assertEquals("18.00", UserWalletTransactionDisplaySupport.formatAmount(new BigDecimal("18")));
        assertEquals("12.50", UserWalletTransactionDisplaySupport.formatAmount(new BigDecimal("12.5")));
    }

    @Test
    void resolveStatusLabel_shouldMapPendingAndSettled() {
        assertEquals(UserWalletTransactionConstant.LABEL_SETTLED,
                UserWalletTransactionDisplaySupport.resolveStatusLabel(
                        UserWalletTransactionConstant.STATUS_COMPLETED));
        assertEquals(UserWalletTransactionConstant.LABEL_PENDING,
                UserWalletTransactionDisplaySupport.resolveStatusLabel(
                        UserWalletTransactionConstant.STATUS_PENDING));
    }

    @Test
    void resolveTitle_shouldTrimAndHandleNull() {
        UserWalletTransaction transaction = new UserWalletTransaction();
        transaction.setTitle("  活动奖励  ");
        assertEquals("活动奖励", UserWalletTransactionDisplaySupport.resolveTitle(transaction));
        assertEquals("", UserWalletTransactionDisplaySupport.resolveTitle(null));
    }
}
