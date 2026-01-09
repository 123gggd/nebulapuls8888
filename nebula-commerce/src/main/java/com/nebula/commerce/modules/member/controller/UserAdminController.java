package com.nebula.commerce.modules.member.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.commerce.infrastructure.web.Result;
import com.nebula.commerce.modules.member.entity.User;
import com.nebula.commerce.modules.member.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/member")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

    private final UserMapper userMapper;

    /**
     * 会员列表查询
     */
    @GetMapping("/list")
    public Result<Page<User>> list(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer size,
                                   @RequestParam(required = false) String username) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 仅查询普通会员，不混淆管理员和商家
        wrapper.eq(User::getRole, "USER");

        if (StringUtils.hasText(username)) {
            wrapper.like(User::getUsername, username);
        }
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> result = userMapper.selectPage(pageParam, wrapper);
        result.getRecords().forEach(u -> u.setPassword(null)); // 脱敏
        return Result.success(result);
    }

    /**
     * 修改用户状态 (封号/解封)
     */
    @PostMapping("/status/{id}/{status}")
    public Result<String> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        // 禁止禁用管理员自己 (防止误操作导致无法登录)
        User user = userMapper.selectById(id);
        if (user != null && "ADMIN".equals(user.getRole())) {
            return Result.error("无法修改管理员状态");
        }

        userMapper.update(null, new LambdaUpdateWrapper<User>()
                .set(User::getStatus, status)
                .eq(User::getId, id));

        return Result.success(status == 1 ? "用户已启用" : "用户已禁用");
    }
}