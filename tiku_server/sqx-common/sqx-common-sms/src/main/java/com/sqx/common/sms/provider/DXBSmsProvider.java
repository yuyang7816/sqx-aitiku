package com.sqx.common.sms.provider;

import com.sqx.common.core.exception.BizException;
import com.sqx.common.core.utils.Md5Utils;
import com.sqx.common.core.utils.http.OkHttpUtil;
import com.sqx.common.redis.utils.ConfigUtil;
import com.sqx.common.sms.domain.ConvertPhone;
import com.sqx.common.sms.enums.SmsError;
import com.sqx.common.sms.enums.SmsProviderEnum;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chengming
 * @since 2025/6/11
 * <p>
 * 短信包服务
 */
@Slf4j
public class DXBSmsProvider implements ISmsProvider {

    @Override
    public String getProviderName() {
        return SmsProviderEnum.DXB.getDesc();
    }

    @Override
    public void sendSms(String phone, String content) {

        // 国内短信地址
        String chinaUrl = "https://api.smsbao.com/sms";

        // 国际短信地址
        String internationalUrl = "https://api.smsbao.com/wsms";

        String username = ConfigUtil.getConfigValue("sms_smsbao_username");
        String password = ConfigUtil.getConfigValue("sms_smsbao_password");

        ConvertPhone convertPhone = convertPhoneNo(phone);

        Map<String, String> params = new HashMap<>();
        params.put("u", username);
        params.put("p", Md5Utils.md5s(password));
        params.put("m", convertPhone.getEncodePhone());
        //params.put("c", Md5Utils.encodeUrlString(content, "UTF-8"));
        params.put("c", content);

        String resp;
        if (convertPhone.getChina()) {
            resp = OkHttpUtil.doGet(chinaUrl, params);
        } else {
            resp = OkHttpUtil.doGet(internationalUrl, params);
        }
        log.info("[sms] - 短信宝返回值: {}", resp);

        if ("0".equals(resp)) {
            log.info("[sms] - 短信发送成功");
            return;
        }

        printDXBError(resp);
        throw new BizException(SmsError.SMS_SEND_ERROR);
    }

    private void printDXBError(String resp) {
        switch (resp) {
            case "30":
                log.error("[sms] - 短信宝密码错误");
                return;
            case "40":
                log.error("[sms] - 短信宝账号不存在");
                return;
            case "41":
                log.error("[sms] - 短信宝余额不足");
                return;
            case "43":
                log.error("[sms] - 短信宝IP地址限制");
                return;
            case "50":
                log.error("[sms] - 短信内容含有敏感词");
                return;
            case "51":
                log.error("[sms] - 短信手机号码不正确");
                return;
            default:
                log.error("[sms] - 未知异常");
        }
    }

    /**
     * 转换手机号
     *
     * @param phone 手机号
     */
    private ConvertPhone convertPhoneNo(String phone) {
        ConvertPhone convertPhone = new ConvertPhone();
        if (phone.contains("+86") || !phone.contains("+")) {
            convertPhone.setEncodePhone(phone.replace("+86", ""));
            convertPhone.setChina(Boolean.TRUE);
        } else {
            try {
                convertPhone.setEncodePhone(URLEncoder.encode(phone, "UTF-8"));
                convertPhone.setChina(Boolean.FALSE);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                throw new BizException("发送失败");
            }
        }

        return convertPhone;
    }
}
