package com.sqx.common.file.storage;

import com.sqx.common.core.exception.BizException;
import com.sqx.common.core.utils.SpringContextHolder;
import com.sqx.common.file.domain.UploadParam;
import com.sqx.common.file.domain.UploadRes;
import com.sqx.common.file.enums.FileError;
import com.sqx.common.file.utils.FileUtil;
import com.sqx.common.redis.utils.ConfigUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * @author chengming
 * @since 2024/10/25
 */
@Slf4j
public class LocalFileStorage implements IFileStorage {

    /**
     * 简易的文件上传
     * @param file 文件
     */
    @Override
    public UploadRes upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(FileError.NONSUPPORT_EMPTY_FILE);
        }

        String uploadPath = normalizeUploadPath(ConfigUtil.getConfigValueOrDefault("file_local_upload_path"));

        try {
            String rootPath = System.getProperty("user.dir");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            String dataFolder = LocalDateTime.now().format(formatter);
            String uploadFolder = rootPath + uploadPath + "/" + dataFolder;
            Path uploadFolderPath = Paths.get(uploadFolder);

            // 确保目标目录存在
            if (!Files.exists(uploadFolderPath)) {
                Files.createDirectories(uploadFolderPath);
            }

            // 重命名文件
            String fileExtension = FileUtil.getFileExtension(file.getOriginalFilename());
            String fileId = UUID.randomUUID().toString();
            String newName = fileId + "." + fileExtension;

            Path path = Paths.get(uploadFolder, newName);
            file.transferTo(path);

            // 返回上传文件的访问路径
            String filePath = uploadPath + "/" + dataFolder + "/" + newName;

            UploadRes uploadRes = new UploadRes();
            uploadRes.setFileId(fileId);
            uploadRes.setRelativePath(filePath);
            uploadRes.setExtension(fileExtension);
            uploadRes.setSize(file.getSize());

            String domain = resolvePublicBaseUrl();
            String accessUrl = domain + filePath;
            uploadRes.setUrl(accessUrl);
            uploadRes.setPath(accessUrl);
            return uploadRes;
        } catch (Exception e) {
            log.error("上传文件失败");
            e.printStackTrace();
            throw new BizException(FileError.UPLOAD_FAILED);
        }
    }

    @Override
    public UploadRes upload(UploadParam uploadParam) {
        return null;
    }

    private String getContextPath() {
        Environment environment = SpringContextHolder.getBean(Environment.class);
        return environment.getProperty("server.servlet.context-path");
    }

    private static String normalizeUploadPath(String uploadPath) {
        if (StringUtils.isBlank(uploadPath)) {
            uploadPath = "/upload";
        }
        return uploadPath.startsWith("/") ? uploadPath : "/" + uploadPath;
    }

    private static String resolvePublicBaseUrl() {
        String baseUrl = normalizeDomain(ConfigUtil.getConfigValueOrDefault("file_public_base_url"));
        if (StringUtils.isNotBlank(baseUrl)) {
            return baseUrl;
        }
        return normalizeDomain(ConfigUtil.getConfigValueOrDefault("sys_h5_domain"));
    }

    private static String normalizeDomain(String domain) {
        if (StringUtils.isBlank(domain)) {
            return "";
        }
        return domain.endsWith("/") ? domain.substring(0, domain.length() - 1) : domain;
    }
}
