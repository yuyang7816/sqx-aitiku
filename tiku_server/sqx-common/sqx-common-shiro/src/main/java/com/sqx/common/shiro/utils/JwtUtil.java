package com.sqx.common.shiro.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * jwt工具类
 *
 */
@ConfigurationProperties(prefix = "sqx.jwt")
@Component
public class JwtUtil {
    private Logger logger = LoggerFactory.getLogger(getClass());


    // 创建对象主体
    private static final String CLAIM_KEY_SUBJECT = "subject";
    // 创建时间
    private static final String CLAIM_KEY_CREATED = "created";

    /** 标准 Authorization 请求头 */
    public static final String AUTHORIZATION_HEADER = "Authorization";

    /** Bearer Token 前缀，完整格式：Authorization: Bearer {token} */
    public static final String BEARER_PREFIX = "Bearer ";

    private String secret;
    private long expire;
    private String header;

    /**
     * 生成jwt token
     */
    public String generateToken(long userId) {
        Date nowDate = new Date();
        //过期时间
        Date expireDate = new Date(nowDate.getTime() + expire * 1000);

        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_CREATED, new Date());

        return Jwts.builder()
                .addClaims(claims)
                .setSubject(String.valueOf(userId))
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public Claims getClaimByToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        }catch (Exception e){
            logger.debug("validate is token error ", e);
            return null;
        }
    }

    /**
     * token是否过期
     * @return  true：过期
     */
    public boolean isTokenExpired(Date expiration) {
        return expiration.before(new Date());
    }

    /**
     * 验证token是否还有效
     *
     * @param token       客户端传入的token
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimByToken(token);
            return !isTokenExpired(claims.getExpiration());
        } catch (Exception e) {
            return false;
        }
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpire() {
        return expire;
    }

    public void setExpire(long expire) {
        this.expire = expire;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    /**
     * 从请求中解析 JWT Token。
     * 优先读取 Authorization: Bearer {token}，兼容 legacy 配置头（sqx.jwt.header）及同名 query 参数。
     */
    public String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.isNotBlank(authorization) && authorization.startsWith(BEARER_PREFIX)) {
            String bearerToken = authorization.substring(BEARER_PREFIX.length()).trim();
            if (StringUtils.isNotBlank(bearerToken)) {
                return bearerToken;
            }
        }

        String token = request.getHeader(header);
        if (StringUtils.isBlank(token)) {
            token = request.getParameter(header);
        }
        return token;
    }
}
