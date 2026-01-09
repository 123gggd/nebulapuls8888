package com.nebula.commerce.modules.system.annotation;

import java.lang.annotation.*;

/**
 * 自定义操作日志注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    /**
     * 模块/功能描述
     */
    String value() default "";
}