package com.sqx.app.base.file.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 本地文件静态访问（映射 /upload/** 至工作目录，上传后可直接 GET 访问）
 */
@Configuration
public class LocalFileResourceConfig implements WebMvcConfigurer {

    @Value("${sqx.file.local-upload-path:/upload}")
    private String localUploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String normalizedPath = localUploadPath.startsWith("/") ? localUploadPath : "/" + localUploadPath;
        String rootPath = System.getProperty("user.dir");
        registry.addResourceHandler(normalizedPath + "/**")
                .addResourceLocations("file:" + rootPath + normalizedPath + "/");
    }
}
