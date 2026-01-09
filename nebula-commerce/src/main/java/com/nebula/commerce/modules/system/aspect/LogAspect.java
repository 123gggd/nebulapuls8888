package com.nebula.commerce.modules.system.aspect;

import cn.hutool.json.JSONUtil;
import com.nebula.commerce.infrastructure.web.SecurityUtils;
import com.nebula.commerce.modules.system.annotation.Log;
import com.nebula.commerce.modules.system.entity.SysLog;
import com.nebula.commerce.modules.system.mapper.SysLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 操作日志切面
 * 修复：移除 Hutool ServletUtil 依赖，解决 javax/jakarta 冲突
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final SysLogMapper sysLogMapper;
    private final SecurityUtils securityUtils;

    @Around("@annotation(logAnno)")
    public Object around(ProceedingJoinPoint point, Log logAnno) throws Throwable {
        long beginTime = System.currentTimeMillis();
        Object result = point.proceed(); // 执行目标方法
        long time = System.currentTimeMillis() - beginTime;

        try {
            saveLog(point, logAnno, time);
        } catch (Exception e) {
            log.error("日志记录异常", e);
        }

        return result;
    }

    private void saveLog(ProceedingJoinPoint point, Log logAnno, long time) {
        // 1. 获取方法签名和请求
        MethodSignature signature = (MethodSignature) point.getSignature();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        SysLog sysLog = new SysLog();
        sysLog.setOperation(logAnno.value());
        sysLog.setMethod(request.getRequestURI());

        // [关键修复] 使用内部方法获取IP，不依赖外部工具类
        sysLog.setIp(getClientIp(request));

        // 2. 获取用户信息
        String username = securityUtils.getCurrentUsername();
        sysLog.setUsername(username != null ? username : "匿名用户");

        // 3. 序列化请求参数 (简单截取)
        Object[] args = point.getArgs();
        try {
            String params = JSONUtil.toJsonStr(args);
            if (params.length() > 2000) {
                params = params.substring(0, 2000) + "...";
            }
            sysLog.setParams(params);
        } catch (Exception e) {
            // 参数无法序列化时不阻断流程
        }

        sysLog.setCreateTime(LocalDateTime.now());
        sysLogMapper.insert(sysLog);
    }

    /**
     * 手动实现获取客户端真实IP
     * 兼容 Nginx 转发 (X-Forwarded-For)
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 对于多级代理，X-Forwarded-For 可能包含多个IP，第一个才是真实的
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        // 本地开发处理
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }
        return ip;
    }
}