package com.sqx.app.base.notification.remote;

import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson2.JSONObject;
import com.sqx.app.base.utils.SysConfigUtil;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.core.utils.http.OkHttpUtil;
import com.sqx.common.redis.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chengming
 * @since 2025/11/18
 * 个推 API
 *
 * <a href="https://docs.getui.com/getui/server/rest_v2/introduction/">个推文档</a>
 */
@Slf4j
@Component
public class GetuiApi {

    private static final String BASE_API = "https://restapi.getui.com/v2/";
    private static final String CONTENT_TYPE = "application/json;charset=utf-8";

    private String getApi() {
        String appid = SysConfigUtil.getConfigValue("sys_getui_appid");
        return BASE_API + appid;
    }

    /**
     * 获取个推token
     * @return token
     */
    private String getToken() {
        String api = getApi() + "/auth";
        String getuiTokenKey = "GetuiToken";

        if (RedisUtil.hasKey(getuiTokenKey)) {
            return RedisUtil.getCacheObject(getuiTokenKey);
        }

        String appkey = SysConfigUtil.getConfigValue("sys_getui_appkey");
        String mastersecret = SysConfigUtil.getConfigValue("sys_getui_mastersecret");
        long timestamp = System.currentTimeMillis();

        Map<String, Object> body = new HashMap<>();
        body.put("sign", SecureUtil.sha256(appkey + timestamp + mastersecret));
        body.put("timestamp", timestamp);
        body.put("appkey", appkey);

        String resp = OkHttpUtil.doPost(api, JSONObject.toJSONString(body), CONTENT_TYPE);
        JSONObject respJson = JSONObject.parseObject(resp);
        Integer code = respJson.getInteger("code");
        if (0 != code) {
            log.error("获取个推token失败, [{}]", respJson.getString("msg"));
            throw new BizException("获取个推token失败");
        }

        JSONObject data = respJson.getJSONObject("data");
        String token = data.getString("token");
        String expireTime = data.getString("expire_time");

        // 计算有效期
        long expireTimeLong = Long.parseLong(expireTime);
        long expireTimes = (expireTimeLong - System.currentTimeMillis()) / 1000;
        RedisUtil.setCacheObject(getuiTokenKey, token, Duration.ofSeconds(expireTimes));

        return token;
    }

    /**
     * 执行cid单推
     * @param msgNo 消息编号
     * @param cid 用户cid
     */
    public void singlePush(String cid, String msgNo, String title, String content, String url) {
        String api = getApi() + "/push/single/cid";

        Map<String, String> headers = new HashMap<>();
        headers.put("token", getToken());

        Map<String, Object> body = new HashMap<>();
        body.put("request_id", msgNo);

        Map<String, Object> audience = new HashMap<>();
        audience.put("cid", Collections.singletonList(cid));
        body.put("audience", audience);

        Map<String, Object> notification = new HashMap<>();
        notification.put("title", title);
        notification.put("body", content);
        notification.put("click_type", "none");
//        notification.put("url", url);

        Map<String, Object> pushMessage = new HashMap<>();
        pushMessage.put("notification", notification);

        body.put("push_message", pushMessage);

        String resp = OkHttpUtil.doPost(api, JSONObject.toJSONString(body), CONTENT_TYPE, headers);
        JSONObject respJson = JSONObject.parseObject(resp);
        Integer code = respJson.getInteger("code");
        if (0 != code) {
            log.error("cid单推失败, 错误消息: [{}], cid: [{}], 消息编号: [{}]", respJson.getString("msg"), cid, msgNo);
            throw new BizException("cid单推失败");
        }
    }
}
