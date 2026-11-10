package com.sqx.app.base.utils.wx;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author chengming
 * @since 2025/11/19
 */
@Data
public class WxMaSubscribeMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 接收者（用户）的 openid
     */
    private String toUser;

    /**
     * 所需下发的订阅模板id
     */
    private String templateId;

    /**
     * 点击模板卡片后的跳转页面，仅限本小程序内的页面。支持带参数,（示例index?foo=bar）。该字段不填则模板无跳转
     */
    private String page;

    /**
     * 模板内容，格式形如{ "phrase3": { "value": "审核通过" }, "name1": { "value": "订阅" }, "date2": { "value": "2019-12-25 09:42" } }
     */
    private List<MsgData> data;

    /**
     * 跳转小程序类型：developer为开发版；trial为体验版；formal为正式版；默认为正式版
     */
    private String miniprogramState = "formal";

    /**
     * 进入小程序查看”的语言类型，支持zh_CN(简体中文)、en_US(英文)、zh_HK(繁体中文)、zh_TW(繁体中文)，默认为zh_CN
     */
    private String lang = "zh_CN";

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MsgData implements Serializable {
        private static final long serialVersionUID = 1L;

        private String name;
        private String value;
    }
}
