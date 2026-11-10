package com.sqx.app.base.poster.support;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.sqx.app.base.poster.constants.ReportPosterConstant;
import com.sqx.app.base.utils.RequestUtil;
import com.sqx.app.base.utils.wx.WxUtil;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.redis.utils.ConfigUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * 生成月度报告分享小程序码
 */
@Slf4j
@Component
public class ReportMiniProgramQrSupport {

    private static final DateTimeFormatter MONTH_SCENE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    @Resource
    private WxUtil wxUtil;

    public String buildReportQrCodeUrl(YearMonth month) {
        YearMonth target = month == null ? YearMonth.now() : month;
        String scene = ReportPosterConstant.SCENE_PREFIX + target.format(MONTH_SCENE_FORMAT);
        String cachedUrl = resolveExistingQrCodeUrl(scene);
        if (StringUtils.isNotBlank(cachedUrl)) {
            return cachedUrl;
        }

        byte[] imageBytes = wxUtil.getWxMaUnlimitedQrCode(
                scene,
                ReportPosterConstant.MA_ENTRY_PAGE,
                RequestUtil.getClientType()
        );
        return saveQrCodeImage(scene, imageBytes);
    }

    String resolveExistingQrCodeUrl(String scene) {
        try {
            String uploadPath = normalizeUploadPath(ConfigUtil.getConfigValueOrDefault("file_local_upload_path"));
            Path target = resolveQrFilePath(uploadPath, System.getProperty("user.dir"), scene);
            if (Files.exists(target) && Files.size(target) > 0) {
                return buildAccessUrl(buildQrRelativePath(uploadPath, toSafeFileName(scene)));
            }
        } catch (Exception ex) {
            log.warn("读取报告海报小程序码失败 scene={}", scene, ex);
        }
        return null;
    }

    String saveQrCodeImage(String scene, byte[] imageBytes) {
        if (isWechatErrorJson(imageBytes)) {
            throw new BizException("小程序码生成失败，请检查微信配置");
        }
        try {
            String uploadPath = normalizeUploadPath(ConfigUtil.getConfigValueOrDefault("file_local_upload_path"));
            Path target = resolveQrFilePath(uploadPath, System.getProperty("user.dir"), scene);
            Files.createDirectories(target.getParent());
            Files.write(target, imageBytes);
            return buildAccessUrl(buildQrRelativePath(uploadPath, toSafeFileName(scene)));
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("保存报告海报小程序码失败 scene={}", scene, ex);
            throw new BizException("小程序码保存失败");
        }
    }

    static String normalizeUploadPath(String uploadPath) {
        if (StringUtils.isBlank(uploadPath)) {
            uploadPath = "/upload";
        }
        return uploadPath.startsWith("/") ? uploadPath : "/" + uploadPath;
    }

    static String toSafeFileName(String scene) {
        return scene.replaceAll("[^A-Za-z0-9_-]", "_") + ".png";
    }

    static String buildQrRelativePath(String uploadPath, String safeFileName) {
        return uploadPath + "/" + ReportPosterConstant.QR_RELATIVE_DIR + "/" + safeFileName;
    }

    static Path resolveQrFilePath(String uploadPath, String rootPath, String scene) {
        Path qrDir = Paths.get(rootPath + uploadPath, ReportPosterConstant.QR_RELATIVE_DIR);
        return qrDir.resolve(toSafeFileName(scene));
    }

    private String buildAccessUrl(String relativePath) {
        String domain = ConfigUtil.getConfigValue("sys_h5_domain");
        if (StringUtils.isBlank(domain)) {
            return relativePath;
        }
        String normalizedDomain = domain.endsWith("/") ? domain.substring(0, domain.length() - 1) : domain;
        return normalizedDomain + relativePath;
    }

    static boolean isWechatErrorJson(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return true;
        }
        if (bytes[0] != '{') {
            return false;
        }
        try {
            JSONObject json = JSON.parseObject(new String(bytes, java.nio.charset.StandardCharsets.UTF_8));
            return json.containsKey("errcode") && json.getIntValue("errcode") != 0;
        } catch (Exception ex) {
            return false;
        }
    }
}
