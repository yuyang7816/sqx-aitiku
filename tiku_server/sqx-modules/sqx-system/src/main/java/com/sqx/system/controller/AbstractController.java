package com.sqx.system.controller;


import com.sqx.common.shiro.model.AuthUser;
import com.sqx.system.db.entity.SysUserEntity;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

/**
 * Controller公共组件
 *
 */
public abstract class AbstractController {
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	protected SysUserEntity getUser() {
		Subject subject = SecurityUtils.getSubject();
		if (subject.isAuthenticated()) {
			Object principal = subject.getPrincipal();
		}

		AuthUser principal = (AuthUser) SecurityUtils.getSubject().getPrincipal();
		SysUserEntity sysUserEntity = new SysUserEntity();
		BeanUtils.copyProperties(principal, sysUserEntity);
		return sysUserEntity;
	}

	protected Long getUserId() {
		return getUser().getUserId();
	}
}
