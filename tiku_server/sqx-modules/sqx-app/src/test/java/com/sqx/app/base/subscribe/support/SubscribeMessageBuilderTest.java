package com.sqx.app.base.subscribe.support;

import com.sqx.app.base.subscribe.constants.SubscribeMessageScene;
import com.sqx.app.base.utils.wx.WxMaSubscribeMessage;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubscribeMessageBuilderTest {

    private final SubscribeMessageBuilder builder = new SubscribeMessageBuilder();

    @Test
    void buildMonthlyReport_shouldIncludePageWithMonthQuery() {
        WxMaSubscribeMessage message = builder.buildMonthlyReport(
                "openid-test",
                "tmpl-monthly",
                "2026年8月学习报告已生成",
                YearMonth.of(2026, 8),
                "超越了 88% 的同龄人",
                "2026-08");
        assertEquals("openid-test", message.getToUser());
        assertEquals("tmpl-monthly", message.getTemplateId());
        assertEquals("package/profile/analysis-report?month=2026-08", message.getPage());
        assertEquals(3, message.getData().size());
    }

    @Test
    void buildVipExpire_shouldUseMemberCenterPage() {
        WxMaSubscribeMessage message = builder.buildVipExpire(
                "openid-vip",
                "tmpl-vip",
                "会员即将到期提醒",
                LocalDateTime.of(2026, 8, 15, 12, 0),
                "续费后可继续使用无限次 AI");
        assertEquals("package/member/center", message.getPage());
        assertTrue(message.getData().stream().anyMatch(item -> "2026-08-15".equals(item.getValue())));
    }

    @Test
    void buildWrongReview_shouldIncludeStaleCount() {
        WxMaSubscribeMessage message = builder.buildWrongReview(
                "openid-wrong",
                "tmpl-wrong",
                "待复习错题提醒",
                5,
                "建议今晚集中重做");
        assertEquals("pages/notebook/notebook", message.getPage());
        assertTrue(message.getData().stream().anyMatch(item -> "5".equals(item.getValue())));
    }

    @Test
    void truncateThing_shouldLimitToTwentyChars() {
        String truncated = builder.truncateThing("这是一段超过二十个汉字长度的订阅消息标题内容需要被截断处理");
        assertEquals(20, truncated.length());
    }

    @Test
    void buildTestMessage_shouldSupportAllScenes() {
        for (SubscribeMessageScene scene : SubscribeMessageScene.values()) {
            WxMaSubscribeMessage message = builder.buildTestMessage("openid", "tmpl", scene);
            assertEquals("openid", message.getToUser());
            assertEquals("tmpl", message.getTemplateId());
            assertTrue(message.getData().size() >= 3);
        }
    }
}
