package com.sqx.app.base.ocr.support;

import com.sqx.common.redis.utils.ConfigUtil;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 将 OCR 图片 URL 解析为本地可读路径
 */
public final class OcrImagePathSupport {

    private OcrImagePathSupport() {
    }

    /**
     * 从图片 URL 解析本地绝对路径（仅支持本地上传目录）
     */
    public static Path resolveLocalPath(String imageUrl) {
        if (StringUtils.isBlank(imageUrl)) {
            return null;
        }
        String uploadPath = ConfigUtil.getConfigValueOrDefault("file_local_upload_path");
        if (StringUtils.isBlank(uploadPath)) {
            uploadPath = "/upload";
        }
        String normalizedUpload = uploadPath.startsWith("/") ? uploadPath : "/" + uploadPath;
        String relative = extractRelativeUploadPath(imageUrl, normalizedUpload);
        if (StringUtils.isBlank(relative)) {
            return null;
        }
        String rootPath = System.getProperty("user.dir");
        Path localPath = Paths.get(rootPath + normalizedUpload, relative.split("/"));
        if (!Files.exists(localPath)) {
            return null;
        }
        return localPath;
    }

    static String extractRelativeUploadPath(String imageUrl, String normalizedUpload) {
        String pathPart = imageUrl;
        if (imageUrl.contains("://")) {
            try {
                pathPart = URI.create(imageUrl).getPath();
            } catch (Exception ignored) {
                pathPart = imageUrl;
            }
        }
        int idx = pathPart.indexOf(normalizedUpload + "/");
        if (idx >= 0) {
            return pathPart.substring(idx + normalizedUpload.length() + 1);
        }
        if (pathPart.startsWith(normalizedUpload + "/")) {
            return pathPart.substring(normalizedUpload.length() + 1);
        }
        return null;
    }
}
