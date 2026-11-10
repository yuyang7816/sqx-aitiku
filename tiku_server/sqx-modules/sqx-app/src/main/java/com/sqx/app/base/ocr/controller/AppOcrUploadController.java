package com.sqx.app.base.ocr.controller;

import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.file.service.FileService;
import com.sqx.common.core.domain.Result;
import com.sqx.common.file.domain.UploadRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * 用户端 OCR 图片上传（POST /app/ocr/upload，需登录）
 */
@Tag(name = "用户端-OCR", description = "文档 21.2：POST /app/ocr/upload 拍照搜题图片上传，见 docs/ocr-api.md")
@RestController
@RequestMapping("/app/ocr")
@SecurityRequirement(name = "appToken")
public class AppOcrUploadController {

    @Resource
    private FileService fileService;

    @Operation(summary = "OCR 图片上传", description = "接收拍照/相册图片（jpg/png 等），返回 url 与 fileId")
    @ApiResponse(content = @Content(schema = @Schema(implementation = UploadRes.class)))
    @Login
    @PostMapping("upload")
    public Result<UploadRes> upload(@RequestParam("file") MultipartFile file) {
        return fileService.uploadImage(file);
    }
}
