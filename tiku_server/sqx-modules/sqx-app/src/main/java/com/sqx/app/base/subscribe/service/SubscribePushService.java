package com.sqx.app.base.subscribe.service;

import com.sqx.app.base.subscribe.domain.bo.SubscribeRegisterBO;
import com.sqx.app.base.subscribe.domain.vo.SubscribeRegisterVO;
import com.sqx.app.base.subscribe.domain.vo.SubscribeTemplateVO;
import com.sqx.app.base.subscribe.domain.vo.SubscribeTestSendVO;

import java.time.LocalDateTime;
import java.time.YearMonth;

/**
 * 微信小程序订阅消息推送
 */
public interface SubscribePushService {

    SubscribeTemplateVO getTemplates();

    SubscribeTestSendVO sendTest(Long userId, String sceneCode);

    SubscribeRegisterVO registerPreferences(Long userId, SubscribeRegisterBO body);

    boolean sendMonthlyReport(Long userId, YearMonth month, String title, String summary);

    boolean sendVipExpire(Long userId, String title, LocalDateTime expireAt, String remark);

    boolean sendWrongReview(Long userId, int staleCount, String title, String remark);
}
