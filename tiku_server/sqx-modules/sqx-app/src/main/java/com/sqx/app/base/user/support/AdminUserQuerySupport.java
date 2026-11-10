package com.sqx.app.base.user.support;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 管理端列表：按用户名/手机号筛选用户、批量加载用户展示字段
 */
public final class AdminUserQuerySupport {

    private AdminUserQuerySupport() {
    }

    public static String resolveDisplayName(UserEntity user) {
        if (user == null) {
            return null;
        }
        if (StringUtils.isNotBlank(user.getNickname())) {
            return user.getNickname().trim();
        }
        if (StringUtils.isNotBlank(user.getUserName())) {
            return user.getUserName().trim();
        }
        return user.getUserId() != null ? "用户" + user.getUserId() : null;
    }

    /**
     * @return null 表示不按用户列表过滤；empty 表示无匹配用户
     */
    public static List<Long> resolveFilterUserIds(IUserService userService, Long userId, String userName, String phone) {
        if (userId != null) {
            return null;
        }
        String nameKeyword = StringUtils.trimToNull(userName);
        String phoneKeyword = StringUtils.trimToNull(phone);
        if (nameKeyword == null && phoneKeyword == null) {
            return null;
        }

        QueryWrapper<UserEntity> userQuery = new QueryWrapper<>();
        userQuery.lambda()
                .and(StringUtils.isNotBlank(nameKeyword), wrapper -> wrapper
                        .like(UserEntity::getUserName, nameKeyword)
                        .or()
                        .like(UserEntity::getNickname, nameKeyword))
                .eq(StringUtils.isNotBlank(phoneKeyword), UserEntity::getPhone, phoneKeyword);

        List<UserEntity> users = userService.list(userQuery);
        if (CollectionUtils.isEmpty(users)) {
            return Collections.emptyList();
        }
        return users.stream().map(UserEntity::getUserId).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static Map<Long, UserEntity> loadUserMap(IUserService userService, Collection<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        List<Long> ids = userIds.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return userService.listByIds(ids).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(UserEntity::getUserId, Function.identity(), (a, b) -> a));
    }

    public static void fillUserDisplay(UserEntity user, UserDisplayTarget target) {
        if (user == null || target == null) {
            return;
        }
        target.setUserName(resolveDisplayName(user));
        target.setPhone(user.getPhone());
        target.setNickname(user.getNickname());
        target.setUserEntity(user);
    }

    public interface UserDisplayTarget {
        void setUserName(String userName);

        void setPhone(String phone);

        void setNickname(String nickname);

        void setUserEntity(UserEntity userEntity);
    }
}
