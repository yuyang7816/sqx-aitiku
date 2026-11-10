package com.sqx.system.controller;


import com.alibaba.fastjson2.JSONObject;
import com.sqx.common.core.domain.Result;
import com.sqx.common.redis.utils.RedisUtil;
import com.sqx.common.shiro.constant.ShiroConstant;
import com.sqx.common.shiro.model.AuthSysMenu;
import com.sqx.common.shiro.utils.JwtUtil;
import com.sqx.system.db.entity.SysMenuEntity;
import com.sqx.system.db.entity.SysUserEntity;
import com.sqx.system.db.mapper.SysUserDao;
import com.sqx.system.domain.form.SysLoginForm;
import com.sqx.system.domain.res.TokenRes;
import com.sqx.system.service.SysCaptchaService;
import com.sqx.system.service.SysMenuService;
import com.sqx.system.service.SysUserService;
import com.sqx.system.service.SysUserTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 登录相关
 *
 */
@Tag(name = "登录相关", description = "登录相关")
@RestController
public class SysLoginController extends AbstractController {
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysUserTokenService sysUserTokenService;
    @Autowired
    private SysCaptchaService sysCaptchaService;
    @Resource
    private SysMenuService sysMenuService;
    @Resource
    private JwtUtil jwtUtil;

    @Value(value = "${sqx.jwt.expire}")
    private Long expire;

    @Autowired
    private SysUserDao sysUserDao;

    /**
     * 验证码
     */
    @Operation(summary = "验证码")
    @GetMapping("captcha.jpg")
    public void captcha(HttpServletResponse response, String uuid) throws IOException {
        response.setHeader("Cache-Control", "no-store, no-cache");
        response.setContentType("image/jpeg");

        //获取图片验证码
        BufferedImage image = sysCaptchaService.getCaptcha(uuid);

        ServletOutputStream out = response.getOutputStream();
        ImageIO.write(image, "jpg", out);
        IOUtils.closeQuietly(out);
    }

    /**
     * 登录
     */
    @Operation(summary = "登录")
    @PostMapping("/sys/login")
    public Result login(@RequestBody SysLoginForm form) throws IOException {
        boolean captcha = sysCaptchaService.validate(form.getUuid(), form.getCaptcha());
        if (!captcha) {
            return Result.failed("验证码不正确");
        }

        //用户信息
        SysUserEntity user = sysUserService.queryByUserName(form.getUsername());

        //账号不存在、密码错误
        if (user == null || !user.getPassword().equals(new Sha256Hash(form.getPassword(), user.getSalt()).toHex())) {
            return Result.failed("账号或密码不正确");
        }

        //账号锁定
        if (user.getStatus() == 0) {
            return Result.failed("账号已被锁定,请联系管理员");
        }

        // 缓存用户菜单
        List<String> permsList = sysUserDao.queryAllPerms(user.getUserId());
        List<AuthSysMenu> menuObjects = new ArrayList<>();
        if (permsList != null) {
			for (String perm : permsList) {
				if (perm != null && StringUtils.isNotBlank(perm.trim())){
					AuthSysMenu menu = new AuthSysMenu();
					menu.setPerms(perm.trim());
					menuObjects.add(menu);
				}
			}
        }
//        List<SysMenuEntity> userMenuList = sysMenuService.getUserMenuList(user.getUserId());
        RedisUtil.setCacheObject(String.format(ShiroConstant.AUTH_USER_MENU, user.getUserId()), JSONObject.toJSONString(menuObjects));

        //生成token
        String token = jwtUtil.generateToken(user.getUserId());

        // 缓存token
        RedisUtil.setCacheObject(String.format(ShiroConstant.SYS_TOKEN, user.getUserId()), token, Duration.ofSeconds(expire));

        TokenRes tokenRes = new TokenRes();
        tokenRes.setToken(token);
        tokenRes.setExpire(Math.toIntExact(expire));
        return Result.success(tokenRes);
    }


    /**
     * 退出
     */
    @Operation(summary = "退出")
    @PostMapping("/sys/logout")
    public Result logout() {
        sysUserTokenService.logout(getUserId());
        return Result.success();
    }

}
