package com.sqx.system.service.impl;

import com.sqx.common.core.domain.Result;
import com.sqx.common.shiro.utils.JwtUtil;
import com.sqx.system.db.service.ISysUserTokenService;
import com.sqx.system.domain.res.TokenRes;
import com.sqx.system.service.SysUserTokenService;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("sysUserTokenService")
public class SysUserTokenServiceImpl implements SysUserTokenService {
	//12小时后过期
	private final static int EXPIRE = 3600 * 12;

	@Autowired
	private ISysUserTokenService sysUserTokenService;
	@Autowired
	private JwtUtil jwtUtil;


	@Override
	public Result createToken(long userId) {
		//生成一个token
		//String token = TokenGenerator.generateValue();
		//
		////当前时间
		//Date now = new Date();
		////过期时间
		//Date expireTime = new Date(now.getTime() + EXPIRE * 1000);

		String generateToken = jwtUtil.generateToken(userId);



		////判断是否生成过token
		//SysUserTokenEntity tokenEntity = sysUserTokenService.getById(userId);
		//if(tokenEntity == null){
		//	tokenEntity = new SysUserTokenEntity();
		//	tokenEntity.setUserId(userId);
		//	tokenEntity.setToken(token);
		//	tokenEntity.setUpdateTime(now);
		//	tokenEntity.setExpireTime(expireTime);
		//
		//	//保存token
		//	sysUserTokenService.save(tokenEntity);
		//}else{
		//	tokenEntity.setToken(token);
		//	tokenEntity.setUpdateTime(now);
		//	tokenEntity.setExpireTime(expireTime);
		//
		//	//更新token
		//	sysUserTokenService.updateById(tokenEntity);
		//}

		TokenRes res = new TokenRes();
		res.setToken(generateToken);
		res.setExpire(EXPIRE);

		return Result.success(res);
	}

	@Override
	public void logout(long userId) {
		SecurityUtils.getSubject().logout();
		//生成一个token
		//String token = TokenGenerator.generateValue();
		//
		////修改token
		//SysUserTokenEntity tokenEntity = new SysUserTokenEntity();
		//tokenEntity.setUserId(userId);
		//tokenEntity.setToken(token);
		//sysUserTokenService.updateById(tokenEntity);
	}
}
