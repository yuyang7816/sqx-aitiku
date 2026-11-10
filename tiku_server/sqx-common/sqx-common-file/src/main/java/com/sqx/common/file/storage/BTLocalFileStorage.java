package com.sqx.common.file.storage;

import com.sqx.common.core.exception.BizException;
import com.sqx.common.file.domain.UploadParam;
import com.sqx.common.file.domain.UploadRes;
import com.sqx.common.file.enums.FileError;
import com.sqx.common.redis.utils.ConfigUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * @author chengming
 * @since 2024/12/5
 * 宝塔文件上传
 */
@Slf4j
public class BTLocalFileStorage implements IFileStorage{

    @Override
    public UploadRes upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BizException(FileError.NONSUPPORT_EMPTY_FILE);
        }

        String baseUrl = ConfigUtil.getConfigValue("file_bt_endpoint");
        String baseDomain = baseUrl.split("://")[1].replace("/", "");
        String uploadPath = String.format("/www/wwwroot/%s/file/uploadPath", baseDomain);

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            String dataFolder = LocalDateTime.now().format(formatter);
            String uploadFolder = uploadPath + File.separator + dataFolder;
            Path uploadFolderPath = Paths.get(uploadFolder);

            // 确保目标目录存在
            if (!Files.exists(uploadFolderPath)) {
                Files.createDirectories(uploadFolderPath);
            }

            // 重命名文件
            String oldName = file.getOriginalFilename();
            String newName = UUID.randomUUID() + oldName.substring(oldName.lastIndexOf("."));

            Path path = Paths.get(uploadFolder, newName);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            // 返回上传文件的访问路径
            String filePath = baseUrl
                    + "/file/uploadPath/"
                    + dataFolder + File.separator
                    + newName;
            UploadRes res = new UploadRes();
            return res;
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
}
