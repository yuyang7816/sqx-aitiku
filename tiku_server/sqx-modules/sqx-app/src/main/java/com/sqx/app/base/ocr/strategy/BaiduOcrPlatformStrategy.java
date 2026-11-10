package com.sqx.app.base.ocr.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sqx.app.base.ocr.enums.OcrError;
import com.sqx.app.base.ocr.support.OcrImagePathSupport;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.core.utils.http.OkHttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 百度通用文字识别 OCR
 */
@Slf4j
public class BaiduOcrPlatformStrategy implements IOcrPlatformStrategy {

    private final String apiKey;
    private final String secretKey;

    public BaiduOcrPlatformStrategy(String apiKey, String secretKey) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
    }

    @Override
    public String recognizeText(String imageUrl) {
        if (StringUtils.isBlank(apiKey) || StringUtils.isBlank(secretKey)) {
            throw new BizException(OcrError.OCR_PLATFORM_NOT_FOUND);
        }
        Path imagePath = OcrImagePathSupport.resolveLocalPath(imageUrl);
        if (imagePath == null) {
            throw new BizException(OcrError.IMAGE_NOT_ACCESSIBLE);
        }
        try {
            byte[] bytes = Files.readAllBytes(imagePath);
            String base64 = Base64.getEncoder().encodeToString(bytes);
            String token = fetchAccessToken();
            String api = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic?access_token=" + token;
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/x-www-form-urlencoded");
            String body = "image=" + URLEncoder.encode(base64, "UTF-8");
            String resp = OkHttpUtil.doPost(api, body, "application/x-www-form-urlencoded", headers);
            return parseWords(resp);
        } catch (BizException ex) {
            throw ex;
        } catch (Exception e) {
            log.error("百度 OCR 识别失败", e);
            throw new BizException(OcrError.RECOGNITION_FAILED);
        }
    }

    private String fetchAccessToken() throws Exception {
        String url = "https://aip.baidubce.com/oauth/2.0/token";
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "client_credentials");
        params.put("client_id", apiKey);
        params.put("client_secret", secretKey);
        String resp = OkHttpUtil.doGet(url, params);
        JSONObject json = JSON.parseObject(resp);
        String token = json.getString("access_token");
        if (StringUtils.isBlank(token)) {
            throw new BizException(OcrError.OCR_PLATFORM_NOT_FOUND);
        }
        return token;
    }

    private String parseWords(String resp) {
        JSONObject json = JSON.parseObject(resp);
        if (json.containsKey("error_code") && !Objects.equals(0, json.getInteger("error_code"))) {
            log.warn("百度 OCR 返回错误: {}", resp);
            throw new BizException(OcrError.RECOGNITION_FAILED);
        }
        JSONArray wordsResult = json.getJSONArray("words_result");
        if (wordsResult == null || wordsResult.isEmpty()) {
            throw new BizException(OcrError.RECOGNITION_FAILED);
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < wordsResult.size(); i++) {
            JSONObject item = wordsResult.getJSONObject(i);
            if (item != null && StringUtils.isNotBlank(item.getString("words"))) {
                if (builder.length() > 0) {
                    builder.append('\n');
                }
                builder.append(item.getString("words").trim());
            }
        }
        String text = builder.toString().trim();
        if (StringUtils.isBlank(text)) {
            throw new BizException(OcrError.RECOGNITION_FAILED);
        }
        return text;
    }
}
