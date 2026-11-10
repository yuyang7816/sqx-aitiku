package com.sqx.app.base.utils.wx;

import cn.hutool.core.codec.Base64;
import com.alibaba.fastjson2.JSONObject;
import com.sqx.app.base.constants.SysConfigConstant;
import com.sqx.app.base.enums.ClientEnum;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.utils.SysConfigUtil;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.core.utils.http.OkHttpUtil;
import com.sqx.common.redis.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.spec.AlgorithmParameterSpec;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author chengming
 * @since 2025/8/5
 */
@Component
@Slf4j
public class WxUtil {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    private static final OkHttpClient QR_HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    /**
     * 微信小程序登录
     *
     * @param appId   appId
     * @param secret  secret
     * @param js_code code
     */
    public WxJsLoginRes wxJsLogin(String appId, String secret, String js_code) {
        String api = "https://api.weixin.qq.com/sns/jscode2session";

        Map<String, String> params = new HashMap<>();
        params.put("appid", appId);
        params.put("secret", secret);
        params.put("js_code", js_code);
        params.put("grant_type", "authorization_code");

        String resp = OkHttpUtil.doGet(api, params, null);
        WxJsLoginRes wxJsLoginRes = JSONObject.parseObject(resp, WxJsLoginRes.class);
        if (Objects.nonNull(wxJsLoginRes.getErrcode()) && 0 != wxJsLoginRes.getErrcode()) {
            log.error("微信小程序登录返回结果: {}", wxJsLoginRes);
            throw new BizException(AppError.WECHAT_CODE_INVALID);
        }

        return wxJsLoginRes;
    }

    /**
     * 微信公众号网页授权：code 换 openId（snsapi_userinfo / snsapi_base）
     */
    public WxMpOAuthRes wxMpOAuth2(String appId, String secret, String code) {
        String api = "https://api.weixin.qq.com/sns/oauth2/access_token";

        Map<String, String> params = new HashMap<>();
        params.put("appid", appId);
        params.put("secret", secret);
        params.put("code", code);
        params.put("grant_type", "authorization_code");

        String resp = OkHttpUtil.doGet(api, params, null);
        WxMpOAuthRes oauthRes = JSONObject.parseObject(resp, WxMpOAuthRes.class);
        if (oauthRes == null) {
            throw new BizException(AppError.WECHAT_CODE_INVALID);
        }
        if (oauthRes.getErrcode() != null && oauthRes.getErrcode() != 0) {
            log.error("微信公众号 OAuth 返回结果: {}", oauthRes);
            throw new BizException(AppError.WECHAT_CODE_INVALID);
        }
        if (StringUtils.isBlank(oauthRes.getOpenid())) {
            throw new BizException(AppError.WECHAT_CODE_INVALID);
        }
        return oauthRes;
    }

    /**
     * 解密手机号
     *
     * @param decryptData 加密手机号（微信返回）
     * @param key         session_key
     * @param iv          iv（微信返回）
     */
    public String decryptS5(String decryptData, String key, String iv) {
        try {
            byte[] encData = Base64.decode(decryptData);
            byte[] ivs = Base64.decode(iv);
            byte[] keys = Base64.decode(key);
            AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivs);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(keys, "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            return new String(cipher.doFinal(encData));
        } catch (Exception e) {
            e.printStackTrace();
            throw new BizException("解密手机号失败");
        }
    }

    /**
     * 该接口用于将code换取用户手机号
     * 说明，每个code只能使用一次，code的有效期为5min
     *
     * <a href="https://developers.weixin.qq.com/miniprogram/dev/OpenApiDoc/user-info/phone-number/getPhoneNumber.html">参考文档</a>
     *
     * @param code   code
     * @param openId openId
     * @return
     */
    public WxUserPhone getUserPhoneNumber(String code, String openId, String clientType) {
        String api = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=";

        String accessToken = getToken(clientType);
        api = api + accessToken;

        Map<String, String> body = new HashMap<>();
        body.put("openid", openId);
        body.put("code", code);
        String resp = OkHttpUtil.doPost(api, JSONObject.toJSONString(body));
        log.info("【获取用户手机号】微信返回值: {}", resp);
        JSONObject respJson = JSONObject.parseObject(resp);
        Integer errcode = respJson.getInteger("errcode");
        if (Objects.isNull(errcode) || 0 != errcode) {
            log.error("【获取用户手机号】失败, 错误信息: {}", respJson.getString("errmsg"));
            throw new BizException("获取手机号失败");
        }

        JSONObject phoneInfo = respJson.getJSONObject("phone_info");

        WxUserPhone wxUserPhone = new WxUserPhone();
        wxUserPhone.setPhoneNumber(phoneInfo.getString("phoneNumber"));
        wxUserPhone.setPurePhoneNumber(phoneInfo.getString("purePhoneNumber"));
        wxUserPhone.setCountryCode(phoneInfo.getString("countryCode"));

        return wxUserPhone;
    }

    private String getStableAccessToken(String appId, String appSecret) {
        String cacheKey = "WX::ACCESS_TOKEN::" + appId;
        if (RedisUtil.hasKey(cacheKey)) {
            return RedisUtil.getCacheObject(cacheKey);
        }

        String api = "https://api.weixin.qq.com/cgi-bin/stable_token";

        Map<String, String> body = new HashMap<>();
        body.put("appid", appId);
        body.put("secret", appSecret);
        body.put("grant_type", "client_credential");

        String resp = OkHttpUtil.doPost(api, JSONObject.toJSONString(body));
        log.info("【获取微信access_token】微信返回值: {}", resp);

        JSONObject respJson = JSONObject.parseObject(resp);
        String accessToken = respJson.getString("access_token");
        if (Objects.isNull(accessToken)) {
            throw new BizException("【获取微信access_token】失败");
        }

        Integer expiresIn = respJson.getInteger("expires_in");
        RedisUtil.setCacheObject(cacheKey, accessToken, Duration.ofSeconds(expiresIn));

        return accessToken;
    }

    public String getToken(String clientType) {
        if (Objects.equals(clientType, ClientEnum.USER.getCode())) {
            String appid = SysConfigUtil.getConfigValue(SysConfigConstant.Sys.SYS_USER_MA_APPID);
            String secret = SysConfigUtil.getConfigValue(SysConfigConstant.Sys.SYS_USER_MA_SECRET);

            return getStableAccessToken(appid, secret);
        }

        log.error("不支持的客户端：[{}]", clientType);
        throw new BizException("不支持的客户端");
    }

    /**
     * 获取不限制数量的小程序码（scene 携带 inviteCode 等参数）
     *
     * @param scene      最大 32 字符，如邀请码
     * @param page       扫码进入页面，如 pages/index/index
     * @param clientType 客户端类型（用于换取 access_token）
     * @return PNG 二进制
     */
    public byte[] getWxMaUnlimitedQrCode(String scene, String page, String clientType) {
        String accessToken = getToken(clientType);
        String api = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=" + accessToken;

        Map<String, Object> body = new HashMap<>();
        body.put("scene", scene);
        body.put("page", page);
        body.put("width", 430);
        body.put("check_path", false);

        RequestBody requestBody = RequestBody.create(JSONObject.toJSONString(body), JSON_MEDIA_TYPE);
        Request request = new Request.Builder().url(api).post(requestBody).build();

        try (Response response = QR_HTTP_CLIENT.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new BizException("小程序码生成失败");
            }

            byte[] bytes = responseBody.bytes();
            if (bytes.length > 0 && bytes[0] == '{') {
                JSONObject respJson = JSONObject.parseObject(new String(bytes, StandardCharsets.UTF_8));
                Integer errcode = respJson.getInteger("errcode");
                if (Objects.isNull(errcode) || errcode != 0) {
                    log.error("【生成小程序码】失败: {}", respJson.getString("errmsg"));
                    throw new BizException("小程序码生成失败");
                }
            }
            return bytes;
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("【生成小程序码】请求异常 scene={}", scene, ex);
            throw new BizException("小程序码生成失败");
        }
    }

    /**
     * 发送订阅消息
     */
    public void sendSubscribeMessage(WxMaSubscribeMessage message, String clientType) {
        String accessToken = getToken(clientType);

        String api = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + accessToken;

        Map<String, Object> body = new HashMap<>();
        body.put("touser", message.getToUser());
        body.put("template_id", message.getTemplateId());

        Map<String, Map<String, String>> jsonMap = new HashMap<>();
        for (WxMaSubscribeMessage.MsgData item : message.getData()) {
            jsonMap.put(item.getName(), Collections.singletonMap("value", item.getValue()));
        }

        body.put("data", jsonMap);

        if (StringUtils.isNotBlank(message.getPage())) {
            body.put("page", message.getPage());
        }
        if (StringUtils.isNotBlank(message.getMiniprogramState())) {
            body.put("miniprogram_state", message.getMiniprogramState());
        }
        if (StringUtils.isNotBlank(message.getLang())) {
            body.put("lang", message.getLang());
        }

        String resp = OkHttpUtil.doPost(api, JSONObject.toJSONString(body));
        JSONObject respJson = JSONObject.parseObject(resp);
        Integer errcode = respJson.getInteger("errcode");
        if (Objects.isNull(errcode) || 0 != errcode) {
            log.error("小程序消息推送失败: [{}]", respJson.getString("errmsg"));
            throw new BizException("小程序消息推送失败");
        }
    }
}
