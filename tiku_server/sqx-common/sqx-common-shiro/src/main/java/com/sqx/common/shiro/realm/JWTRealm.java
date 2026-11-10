package com.sqx.common.shiro.realm;

import com.sqx.common.core.enums.SystemErrorCode;
import com.sqx.common.redis.utils.RedisUtil;
import com.sqx.common.shiro.constant.ShiroConstant;
import com.sqx.common.shiro.model.AuthUser;
import com.sqx.common.shiro.service.IShiroUserService;
import com.sqx.common.shiro.service.ShiroService;
import com.sqx.common.shiro.token.JWTToken;
import com.sqx.common.shiro.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.Set;

/**
 * @author chengming
 * @since 2025/4/30
 */
@Slf4j
@Component
public class JWTRealm extends AuthorizingRealm {

    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private ShiroService shiroService;
    @Resource
    private IShiroUserService iShiroUserService;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JWTToken;
    }

    /**
     * 查询用户权限信息. 真正的授权在SecurityManager和Authorizer组件内部
     * @param principals the primary identifying principals of the AuthorizationInfo that should be retrieved.
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        AuthUser sysUser = (AuthUser) principals.getPrimaryPrincipal();

        // 查询用户的身份和权限
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        Set<String> userPermissions = shiroService.getUserPermissions(sysUser.getUserId());
        info.addStringPermissions(userPermissions);
        return info;
    }

    /**
     * 认证
     * @param authenticationToken the authentication token containing the user's principal and credentials.
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        String token = (String) authenticationToken.getCredentials(); // 重写了该类，实际上返回的是token
        if (StringUtils.isBlank(token) || !jwtUtil.validateToken(token)) {
            log.error("token校验失败: {}", token);
            throw new AuthenticationException(SystemErrorCode.UNAUTHORIZED.getMessage());
        }

        String userId;
        try {
            // 根据token获得登录用户的id
            Claims claims = jwtUtil.getClaimByToken(token);
            userId = (String) claims.get("sub");
        } catch (Exception e) {
            throw new AuthenticationException(SystemErrorCode.UNAUTHORIZED.getMessage());
        }

        if (!RedisUtil.hasKey(String.format(ShiroConstant.SYS_TOKEN, Long.parseLong(userId)))) {
            throw new AuthenticationException(SystemErrorCode.UNAUTHORIZED.getMessage());
        }

        AuthUser sysUser = iShiroUserService.loadByUserId(Long.parseLong(userId));
        if (Objects.isNull(sysUser)) {
            throw new AuthenticationException(SystemErrorCode.USERNAME_ERROR.getMessage());
        }

        if(0 == sysUser.getStatus()){
            throw new AuthenticationException(SystemErrorCode.DISABLE_USER.getMessage());
        }

        return new SimpleAuthenticationInfo(sysUser, token, this.getName());
    }
}
