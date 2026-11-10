package com.sqx.common.shiro.exception;

import com.sqx.common.core.domain.Result;
import com.sqx.common.core.enums.SystemErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author chengming
 * @since 2025/4/30
 */
@Slf4j
@RestControllerAdvice
public class ShiroExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    public Result<String> handleUnauthorizedException(UnauthorizedException e) {
        log.error(e.getMessage(), e);
        return Result.failed(SystemErrorCode.FORBIDDEN.getMessage());
    }

    @ExceptionHandler(AuthorizationException.class)
    public Result<String> handleAuthorizationException(AuthorizationException e){
        log.error(e.getMessage(), e);
        return Result.failed(SystemErrorCode.FORBIDDEN.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public Result<String> handleAuthenticationException(AuthenticationException e) {
        log.error(e.getMessage(), e);
        return Result.failed(SystemErrorCode.UNAUTHORIZED.getMessage());
    }
}
