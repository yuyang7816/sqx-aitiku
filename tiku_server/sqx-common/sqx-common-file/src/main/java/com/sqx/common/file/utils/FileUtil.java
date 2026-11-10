package com.sqx.common.file.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * @author chengming
 * @since 2025/4/29
 */
public class FileUtil {

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp", "bmp"
    ));

    /**
     * 获取文件扩展名
     * @param filename 文件名
     * @return 文件扩展名
     */
    public static String getFileExtension(String filename) {
        if (StringUtils.isBlank(filename)) {
            return "";
        }

        int dotIndex = filename.lastIndexOf(46);
        return -1 == dotIndex ? "" : filename.substring(dotIndex + 1);
    }

    /**
     * 判断是否为允许上传的图片扩展名
     */
    public static boolean isAllowedImageExtension(String filename) {
        String extension = getFileExtension(filename);
        if (StringUtils.isBlank(extension)) {
            return false;
        }
        return ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT));
    }
}
