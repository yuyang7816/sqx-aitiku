package com.sqx.app.referral.invitecode.support;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.sqx.app.base.utils.RequestUtil;
import com.sqx.app.base.utils.wx.WxUtil;
import com.sqx.app.referral.invitecode.constants.ReferralInviteCodeConstant;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.redis.utils.ConfigUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 生成带 scene(inviteCode) 的小程序码并保存至本地 upload 目录。
 * 供推广邀请与模块十四推广海报（uniapp poster?scene=promo）共用。
 */
@Slf4j
@Component
public class ReferralMiniProgramQrSupport {

    @Resource
    private WxUtil wxUtil;

    /**
     * 返回带 scene(inviteCode) 的小程序码图片 URL；本地已有 PNG 时直接复用，避免重复调用微信接口。
     */
    public String buildQrCodeUrl(String inviteCode) {
        if (StringUtils.isBlank(inviteCode)) {
            throw new BizException("邀请码不能为空");
        }

        String scene = inviteCode.trim();
        if (scene.length() > ReferralInviteCodeConstant.SCENE_MAX_LENGTH) {
            throw new BizException("邀请码长度超出小程序 scene 限制");
        }

        String cachedUrl = resolveExistingQrCodeUrl(scene);
        if (StringUtils.isNotBlank(cachedUrl)) {
            return cachedUrl;
        }

        byte[] imageBytes = wxUtil.getWxMaUnlimitedQrCode(
                scene,
                ReferralInviteCodeConstant.MA_ENTRY_PAGE,
                RequestUtil.getClientType()
        );
        return saveQrCodeImage(scene, imageBytes);
    }

    /**
     * 若本地已存在该邀请码对应的小程序码 PNG，则返回可访问 URL
     */
    String resolveExistingQrCodeUrl(String inviteCode) {
        try {
            String uploadPath = normalizeUploadPath(ConfigUtil.getConfigValueOrDefault("file_local_upload_path"));
            Path target = resolveQrFilePath(uploadPath, System.getProperty("user.dir"), inviteCode);
            if (Files.exists(target) && Files.size(target) > 0) {
                String relativePath = buildQrRelativePath(uploadPath, toSafeFileName(inviteCode));
                return buildAccessUrl(relativePath);
            }
        } catch (Exception ex) {
            log.warn("读取已缓存小程序码失败 inviteCode={}", inviteCode, ex);
        }
        return null;
    }

    String saveQrCodeImage(String inviteCode, byte[] imageBytes) {
        try {
            String uploadPath = normalizeUploadPath(ConfigUtil.getConfigValueOrDefault("file_local_upload_path"));
            Path target = resolveQrFilePath(uploadPath, System.getProperty("user.dir"), inviteCode);
            Files.createDirectories(target.getParent());
            Files.write(target, imageBytes);

            String relativePath = buildQrRelativePath(uploadPath, toSafeFileName(inviteCode));
            return buildAccessUrl(relativePath);
        } catch (BizException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("保存推广小程序码失败 inviteCode={}", inviteCode, ex);
            throw new BizException("小程序码保存失败");
        }
    }

    static String normalizeUploadPath(String uploadPath) {
        if (StringUtils.isBlank(uploadPath)) {
            uploadPath = "/upload";
        }
        return uploadPath.startsWith("/") ? uploadPath : "/" + uploadPath;
    }

    static String toSafeFileName(String inviteCode) {
        return inviteCode.replaceAll("[^A-Za-z0-9]", "_") + ".png";
    }

    static String buildQrRelativePath(String uploadPath, String safeFileName) {
        return uploadPath + "/" + ReferralInviteCodeConstant.QR_RELATIVE_DIR + "/" + safeFileName;
    }

    static Path resolveQrFilePath(String uploadPath, String rootPath, String inviteCode) {
        Path qrDir = Paths.get(rootPath + uploadPath, ReferralInviteCodeConstant.QR_RELATIVE_DIR);
        return qrDir.resolve(toSafeFileName(inviteCode));
    }

    private String buildAccessUrl(String relativePath) {
        String domain = ConfigUtil.getConfigValue("sys_h5_domain");
        if (StringUtils.isBlank(domain)) {
            return relativePath;
        }
        String normalizedDomain = domain.endsWith("/") ? domain.substring(0, domain.length() - 1) : domain;
        return normalizedDomain + relativePath;
    }

    /**
     * 校验微信返回体是否为 JSON 错误（供单元测试）
     */
    static boolean isWechatErrorJson(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return true;
        }
        String head = new String(bytes, 0, Math.min(bytes.length, 1));
        if (!"{".equals(head)) {
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
