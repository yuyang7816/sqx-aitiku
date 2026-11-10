package com.sqx.common.shiro.service.impl;


import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.sqx.common.redis.utils.RedisUtil;
import com.sqx.common.shiro.constant.ShiroConstant;
import com.sqx.common.shiro.model.AuthSysMenu;
import com.sqx.common.shiro.service.ShiroService;
import com.sqx.common.shiro.service.UserPermissionResolver;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author chengming
 * @since 2024/7/11
 */
@Slf4j
@Service
public class ShiroServiceImpl implements ShiroService {

    @Autowired(required = false)
    private UserPermissionResolver userPermissionResolver;

    @Override
    public Set<String> getUserPermissions(long userId) {
        Set<String> permsSet = parsePermissionSet(readMenuCacheJson(userId));
        if (!permsSet.isEmpty()) {
            return permsSet;
        }
        if (userPermissionResolver != null) {
            return userPermissionResolver.resolvePermissions(userId);
        }
        return permsSet;
    }

    private String readMenuCacheJson(long userId) {
        String cacheKey = String.format(ShiroConstant.AUTH_USER_MENU, userId);
        try {
            String menuListJson = RedisUtil.getCacheObject(cacheKey);
            if (StringUtils.isNotBlank(menuListJson)) {
                return menuListJson;
            }
        } catch (Exception ex) {
            log.warn("读取 Redis 权限缓存失败，尝试纯字符串回退: userId={}", userId, ex);
        }
        return RedisUtil.getCacheString(cacheKey);
    }

    private Set<String> parsePermissionSet(String menuListJson) {
        List<String> permsList = new ArrayList<>();
        if (StringUtils.isNotBlank(menuListJson)) {
            List<AuthSysMenu> menuList = JSONObject.parseObject(menuListJson, new TypeReference<List<AuthSysMenu>>() {});
            if (menuList != null) {
                for (AuthSysMenu menu : menuList) {
                    if (StringUtils.isNotBlank(menu.getPerms())) {
                        permsList.add(menu.getPerms());
                    }
                }
            }
        }
        Set<String> permsSet = new HashSet<>();
        for (String perms : permsList) {
            if (StringUtils.isBlank(perms)) {
                continue;
            }
            permsSet.addAll(Arrays.asList(perms.trim().split(",")));
        }
        return permsSet;
    }
}
