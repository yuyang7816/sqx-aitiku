package com.sqx.app.base.user.badge.support;

import com.sqx.app.base.user.badge.domain.entity.UserBadgeDefinition;

import java.util.List;

/**
 * 供勋章判定读取启用中的里程碑定义
 */
public interface UserBadgeDefinitionProvider {

    List<UserBadgeDefinition> listActiveDefinitions();
}
