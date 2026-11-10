package com.sqx.app.wallet.transaction.support;

import com.sqx.app.wallet.transaction.constants.UserWalletTransactionConstant;
import com.sqx.app.wallet.transaction.domain.entity.UserWalletTransaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * 收益流水展示字段转换（时间、金额、结算状态）
 */
public final class UserWalletTransactionDisplaySupport {

    private static final DateTimeFormatter RECORD_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private UserWalletTransactionDisplaySupport() {
    }

    public static String formatRecordTime(LocalDateTime createTime) {
        if (createTime == null) {
            return "";
        }
        return RECORD_TIME.format(createTime);
    }

    public static String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    public static String resolveStatusLabel(Integer status) {
        if (Objects.equals(status, UserWalletTransactionConstant.STATUS_COMPLETED)) {
            return UserWalletTransactionConstant.LABEL_SETTLED;
        }
        if (Objects.equals(status, UserWalletTransactionConstant.STATUS_PENDING)) {
            return UserWalletTransactionConstant.LABEL_PENDING;
        }
        return "";
    }

    public static String resolveTitle(UserWalletTransaction transaction) {
        if (transaction == null || transaction.getTitle() == null) {
            return "";
        }
        return transaction.getTitle().trim();
    }
}
