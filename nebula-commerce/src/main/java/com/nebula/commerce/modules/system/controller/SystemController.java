package com.nebula.commerce.modules.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.commerce.infrastructure.web.Result;
import com.nebula.commerce.modules.system.entity.SysLog;
import com.nebula.commerce.modules.system.mapper.SysLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/system")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SystemController {

    private final SysLogMapper sysLogMapper;

    @Value("${app.admin-invite-code:Nebula2026}")
    private String adminInviteCode;

    @GetMapping("/log/list")
    public Result<Page<SysLog>> listLogs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate
    ) {
        Page<SysLog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SysLog> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(username)) {
            wrapper.like(SysLog::getUsername, username); // 改为模糊查询更友好
        }
        if (startDate != null) {
            wrapper.ge(SysLog::getCreateTime, startDate);
        }
        if (endDate != null) {
            wrapper.le(SysLog::getCreateTime, endDate);
        }

        wrapper.orderByDesc(SysLog::getCreateTime);
        return Result.success(sysLogMapper.selectPage(pageParam, wrapper));
    }

    @GetMapping("/invite-code")
    public Result<String> getInviteCode() {
        return Result.success(adminInviteCode);
    }
}
