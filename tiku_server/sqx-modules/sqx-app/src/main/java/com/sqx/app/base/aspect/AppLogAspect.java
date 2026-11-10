package com.sqx.app.base.aspect;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.util.*;

/**
 * @author chengming
 * @since 2024/7/11
 *
 * APP模块日志
 */

@Aspect
@Component
public class AppLogAspect {
    private final Logger logger = LoggerFactory.getLogger(AppLogAspect.class);

    @Before("execution(* com.sqx.app..controller..*(..))" +
            "&& !execution(* com.sqx.app.base.pay..controller..WxPayController.notifyUrl(..))")
    public void logRequest(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        };

        HttpServletRequest request = attributes.getRequest();

        // 打印基本信息
        logger.info("请求地址: " + request.getRequestURL().toString());
        logger.info("HTTP方法: " + request.getMethod());
        logger.info("IP地址: " + getClientIp(request));
        logger.info("请求方法: {}.{}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());

        // 打印参数
        try {
            Object[] args = joinPoint.getArgs();
            if (null == args || 0 == args.length) {
                logger.info("参数: {}", "{}");
            } else {
                for (Object arg : args) {
                    if (Objects.isNull(arg)) {
                        continue;
                    }

                    // 跳过 Servlet API 对象，避免反射调用危险方法
                    if (arg instanceof ServletRequest || arg instanceof ServletResponse) {
                        logger.info("参数类型: {}, 值: [Servlet API object, 跳过]", arg.getClass().getSimpleName());
                        continue;
                    }

                    // 忽略 MultipartFile 等特殊类型
                    if (arg instanceof MultipartFile || arg instanceof Part) {
                        logger.info("参数类型: {}, 值: 文件上传", arg.getClass().getSimpleName());
                        continue;
                    }

                    logger.info("参数: {}", JSONObject.toJSONString(arg));
                }
            }
        } catch (Exception e) {
            logger.warn("打印参数失败", e);
        }
    }

    // 获取客户端真实 IP（考虑代理）
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        return ip;
    }
}
