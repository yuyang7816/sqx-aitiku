package com.sqx.system.aspect.annotation;

import java.lang.annotation.*;

/**
 * @author chengming
 * @since 2024/7/11
 *
 * 系统日志注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SysLog {

    String value() default "";
}
