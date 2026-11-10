package com.sqx.app.base.file.service;

import com.sqx.app.referral.constants.ReferralConstant;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.redis.utils.ConfigUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * 用户端海报等场景：代理拉取 OSS/CDN 图片，避免 H5 canvas 跨域无法绘制。
 */
@Service
public class AppImageProxyService {

    private static final int CONNECT_TIMEOUT_MS = 8000;
    private static final int READ_TIMEOUT_MS = 20000;
    private static final int MAX_BYTES = 10 * 1024 * 1024;

    public void proxyImage(String rawUrl, HttpServletResponse response) throws IOException {
        String url = StringUtils.trimToNull(rawUrl);
        if (url == null) {
            throw new BizException("图片地址不能为空");
        }
        if (!isAllowedUrl(url)) {
            throw new BizException("不允许代理该图片地址");
        }

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestMethod("GET");
        int status = connection.getResponseCode();
        if (status < 200 || status >= 400) {
            connection.disconnect();
            throw new BizException("图片下载失败");
        }

        String contentType = StringUtils.defaultIfBlank(connection.getContentType(), guessContentType(url));
        response.setContentType(contentType);
        response.setHeader("Cache-Control", "public, max-age=3600");

        try (InputStream input = connection.getInputStream();
             OutputStream output = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int total = 0;
            int read;
            while ((read = input.read(buffer)) != -1) {
                total += read;
                if (total > MAX_BYTES) {
                    throw new BizException("图片体积过大");
                }
                output.write(buffer, 0, read);
            }
            output.flush();
        } finally {
            connection.disconnect();
        }
    }

    public boolean isAllowedUrl(String rawUrl) {
        try {
            URI uri = URI.create(StringUtils.trim(rawUrl));
            String scheme = StringUtils.lowerCase(uri.getScheme());
            if (!"http".equals(scheme) && !"https".equals(scheme)) {
                return false;
            }
            String host = StringUtils.lowerCase(uri.getHost());
            if (StringUtils.isBlank(host)) {
                return false;
            }
            return resolveAllowedHosts().contains(host);
        } catch (Exception ex) {
            return false;
        }
    }

    private Set<String> resolveAllowedHosts() {
        Set<String> hosts = new LinkedHashSet<>();
        addHostFromValue(hosts, ConfigUtil.getConfigValueOrDefault("file_cdn_domain"));
        addHostFromValue(hosts, ConfigUtil.getConfigValueOrDefault("file_public_base_url"));
        addHostFromValue(hosts, ConfigUtil.getConfigValueOrDefault("sys_h5_domain"));
        addHostFromValue(hosts, ConfigUtil.getConfigValueOrDefault(ReferralConstant.poster.SYS_POSTER_IMAGE));
        hosts.add("localhost");
        hosts.add("127.0.0.1");
        return hosts;
    }

    private void addHostFromValue(Set<String> hosts, String value) {
        if (StringUtils.isBlank(value)) {
            return;
        }
        try {
            String normalized = value.trim();
            if (!normalized.contains("://")) {
                normalized = "https://" + normalized.replaceAll("^/+", "");
            }
            URI uri = URI.create(normalized);
            if (StringUtils.isNotBlank(uri.getHost())) {
                hosts.add(uri.getHost().toLowerCase(Locale.ROOT));
            }
        } catch (Exception ignored) {
            // ignore invalid config
        }
    }

    private String guessContentType(String url) {
        String lower = StringUtils.lowerCase(url);
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        if (lower.endsWith(".gif")) {
            return "image/gif";
        }
        return "image/png";
    }
}
