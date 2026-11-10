package com.sqx.system.support;

import com.sqx.common.shiro.service.UserPermissionResolver;
import com.sqx.system.db.mapper.SysUserDao;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DbUserPermissionResolver implements UserPermissionResolver {

    @Resource
    private SysUserDao sysUserDao;

    @Override
    public Set<String> resolvePermissions(long userId) {
        List<String> permsList = sysUserDao.queryAllPerms(userId);
        Set<String> permsSet = new HashSet<>();
        if (permsList == null) {
            return permsSet;
        }
        for (String perms : permsList) {
            if (StringUtils.isBlank(perms)) {
                continue;
            }
            permsSet.addAll(Arrays.asList(perms.trim().split(",")));
        }
        return permsSet;
    }
}
