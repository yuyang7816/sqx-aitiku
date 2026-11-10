package com.sqx.system.service;


import com.sqx.common.core.domain.Result;

/**
 * 用户Token
 *
 */
public interface SysUserTokenService  {

	/**
	 * 生成token
	 * @param userId  用户ID
	 */
	Result createToken(long userId);

	/**
	 * 退出，修改token值
	 * @param userId  用户ID
	 */
	void logout(long userId);

}
