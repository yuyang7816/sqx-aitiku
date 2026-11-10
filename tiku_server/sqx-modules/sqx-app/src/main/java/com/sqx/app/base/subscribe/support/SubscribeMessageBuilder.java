package com.sqx.app.base.subscribe.support;

import com.sqx.app.base.subscribe.constants.SubscribeMessageConstant;
import com.sqx.app.base.subscribe.constants.SubscribeMessageScene;
import com.sqx.app.base.utils.wx.WxMaSubscribeMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 按业务场景组装微信小程序订阅消息体
 */
@Component
public class SubscribeMessageBuilder {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.CHINA);
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.CHINA);
    private static final DateTimeFormatter MONTH_FORMAT =
            DateTimeFormatter.ofPattern("yyyy年M月", Locale.CHINA);

    public WxMaSubscribeMessage buildMonthlyReport(String openId,
                                                   String templateId,
                                                   String title,
                                                   YearMonth month,
                                                   String summary,
                                                   String reportMonthQuery) {
        WxMaSubscribeMessage message = baseMessage(openId, templateId);
        message.setPage(buildReportPage(reportMonthQuery));
        List<WxMaSubscribeMessage.MsgData> data = new ArrayList<>();
        data.add(field(SubscribeMessageConstant.FIELD_TITLE, truncateThing(title)));
        data.add(field(SubscribeMessageConstant.FIELD_TIME, formatMonth(month)));
        data.add(field(SubscribeMessageConstant.FIELD_REMARK, truncateThing(summary)));
        message.setData(data);
        return message;
    }

    public WxMaSubscribeMessage buildVipExpire(String openId,
                                               String templateId,
                                               String title,
                                               LocalDateTime expireAt,
                                               String remark) {
        WxMaSubscribeMessage message = baseMessage(openId, templateId);
        message.setPage(SubscribeMessageConstant.PAGE_VIP_MEMBER);
        List<WxMaSubscribeMessage.MsgData> data = new ArrayList<>();
        data.add(field(SubscribeMessageConstant.FIELD_TITLE, truncateThing(title)));
        data.add(field(SubscribeMessageConstant.FIELD_DATE, formatDate(expireAt)));
        data.add(field(SubscribeMessageConstant.FIELD_REMARK, truncateThing(remark)));
        message.setData(data);
        return message;
    }

    public WxMaSubscribeMessage buildWrongReview(String openId,
                                                 String templateId,
                                                 String title,
                                                 int staleCount,
                                                 String remark) {
        WxMaSubscribeMessage message = baseMessage(openId, templateId);
        message.setPage(SubscribeMessageConstant.PAGE_WRONG_REVIEW);
        List<WxMaSubscribeMessage.MsgData> data = new ArrayList<>();
        data.add(field(SubscribeMessageConstant.FIELD_TITLE, truncateThing(title)));
        data.add(field(SubscribeMessageConstant.FIELD_NUMBER, String.valueOf(Math.max(staleCount, 1))));
        data.add(field(SubscribeMessageConstant.FIELD_REMARK, truncateThing(remark)));
        message.setData(data);
        return message;
    }

    public WxMaSubscribeMessage buildTestMessage(String openId,
                                                 String templateId,
                                                 SubscribeMessageScene scene) {
        LocalDateTime now = LocalDateTime.now();
        switch (scene) {
            case MONTHLY_REPORT:
                return buildMonthlyReport(openId, templateId,
                        "测试月报通知",
                        YearMonth.from(now),
                        "这是一条测试订阅消息",
                        now.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            case VIP_EXPIRE:
                return buildVipExpire(openId, templateId,
                        "测试会员到期提醒",
                        now.plusDays(3),
                        "这是一条测试订阅消息");
            case WRONG_REVIEW:
                return buildWrongReview(openId, templateId,
                        "测试错题复习提醒",
                        3,
                        "这是一条测试订阅消息");
            default:
                throw new IllegalArgumentException("unsupported scene: " + scene);
        }
    }

    private WxMaSubscribeMessage baseMessage(String openId, String templateId) {
        WxMaSubscribeMessage message = new WxMaSubscribeMessage();
        message.setToUser(openId);
        message.setTemplateId(templateId);
        message.setMiniprogramState("formal");
        message.setLang("zh_CN");
        return message;
    }

    private WxMaSubscribeMessage.MsgData field(String name, String value) {
        return new WxMaSubscribeMessage.MsgData(name, StringUtils.defaultString(value));
    }

    String buildReportPage(String reportMonthQuery) {
        if (StringUtils.isBlank(reportMonthQuery)) {
            return SubscribeMessageConstant.PAGE_MONTHLY_REPORT;
        }
        return SubscribeMessageConstant.PAGE_MONTHLY_REPORT + "?month=" + reportMonthQuery.trim();
    }

    String truncateThing(String text) {
        String normalized = StringUtils.trimToEmpty(text);
        if (normalized.length() <= 20) {
            return normalized.isEmpty() ? "学习提醒" : normalized;
        }
        return normalized.substring(0, 20);
    }

    String formatMonth(YearMonth month) {
        if (month == null) {
            return YearMonth.now().format(MONTH_FORMAT);
        }
        return month.format(MONTH_FORMAT);
    }

    String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return LocalDateTime.now().format(DATE_FORMAT);
        }
        return dateTime.format(DATE_FORMAT);
    }

    String formatNowTime() {
        return LocalDateTime.now().format(TIME_FORMAT);
    }
}
