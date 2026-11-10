package com.sqx.common.core.utils.http;

import com.sqx.common.core.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author chengming
 * @since 2025/5/13
 */
@Slf4j
public class OkHttpUtil {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final MediaType FORM = MediaType.get("application/x-www-form-urlencoded");

    private static final OkHttpClient client;

    static {
        // 初始化 OkHttpClient（可自定义配置）
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * GET 请求
     *
     * @param url 请求地址
     * @return 响应字符串
     */
    public static String doGet(String url) {
        return doGet(url, null, null);
    }

    /**
     * GET 请求（带 params）
     *
     * @param url   请求地址
     * @param params 参数 Map
     * @return 响应字符串
     */
    public static String doGet(String url, Map<String, String> params) {
        return doGet(url, params, null);
    }

    /**
     * GET 请求（带 params 和 headers）
     *
     * @param url     请求地址
     * @param params  参数 Map
     * @param headers 请求头 Map
     * @return 响应字符串
     */
    public static String doGet(String url, Map<String, String> params, Map<String, String> headers) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();

        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
            }
        }

        Request.Builder requestBuilder = new Request.Builder().url(urlBuilder.build());

        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.header(entry.getKey(), entry.getValue());
            }
        }

        Request request = requestBuilder.build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new BizException("错误的code: " + response);
            }

            ResponseBody body = response.body();
            if (body == null) {
                log.error("请求失败，请求体为空");
                throw new BizException("请求失败");
            }

            return body.string();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BizException("请求失败");
        }
    }

    /**
     * POST 请求（默认为 application/json 类型）
     *
     * @param url  请求地址
     * @param json 请求体（JSON 字符串）
     * @return 响应字符串
     */
    public static String doPost(String url, String json) {
        return doPost(url, json, null, null);
    }

    /**
     * POST 请求（指定 content-type）
     *
     * @param url         请求地址
     * @param body        请求体
     * @param contentType 内容类型（如 application/json、application/x-www-form-urlencoded）
     * @return 响应字符串
     */
    public static String doPost(String url, String body, String contentType) {
        return doPost(url, body, contentType, null);
    }

    /**
     * POST 请求（支持自定义 content-type 和 headers）
     *
     * @param url         请求地址
     * @param body        请求体
     * @param contentType 内容类型
     * @param headers     请求头
     * @return 响应字符串
     */
    public static String doPost(String url, String body, String contentType, Map<String, String> headers) {
        MediaType mediaType = contentType != null ? MediaType.get(contentType) : JSON;

        RequestBody requestBody = RequestBody.create(body, mediaType);

        Request.Builder requestBuilder = new Request.Builder().url(url).post(requestBody);

        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.header(entry.getKey(), entry.getValue());
            }
        }

        Request request = requestBuilder.build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new BizException("错误的code: " + response);
            }

            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                log.error("请求失败，请求体为空");
                throw new BizException("请求失败");
            }

            return responseBody.string();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BizException("请求失败");
        }
    }

    /**
     * POST 表单提交（application/x-www-form-urlencoded）
     *
     * @param url  请求地址
     * @param form 表单参数
     * @return 响应字符串
     */
    public static String doPostForm(String url, Map<String, String> form) {
        return doPostForm(url, form, null);
    }

    /**
     * POST 表单提交（application/x-www-form-urlencoded）+ headers
     *
     * @param url     请求地址
     * @param form    表单参数
     * @param headers 请求头
     * @return 响应字符串
     */
    public static String doPostForm(String url, Map<String, String> form, Map<String, String> headers) {
        FormBody.Builder formBuilder = new FormBody.Builder();

        for (Map.Entry<String, String> entry : form.entrySet()) {
            formBuilder.add(entry.getKey(), entry.getValue());
        }

        RequestBody body = formBuilder.build();

        Request.Builder requestBuilder = new Request.Builder().url(url).post(body);

        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.header(entry.getKey(), entry.getValue());
            }
        }

        Request request = requestBuilder.build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new BizException("错误的code: " + response);
            }

            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                log.error("请求失败，请求体为空");
                throw new BizException("请求失败");
            }

            return responseBody.string();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BizException("请求失败");
        }
    }
}
