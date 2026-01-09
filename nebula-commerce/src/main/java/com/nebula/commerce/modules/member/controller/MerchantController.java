package com.nebula.commerce.modules.member.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.commerce.infrastructure.web.Result;
import com.nebula.commerce.modules.member.entity.User;
import com.nebula.commerce.modules.member.mapper.UserMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 商家管理控制器
 * 仅限平台管理员 (ADMIN) 访问
 */
@RestController
@RequestMapping("/api/admin/merchant")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class MerchantController {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 创建商家账号
     */
    @PostMapping("/create")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> createMerchant(@RequestBody MerchantCreateReq req) {
        if (!StringUtils.hasText(req.getUsername()) || !StringUtils.hasText(req.getPassword())) {
            return Result.error("用户名和密码不能为空");
        }

        // 1. 检查用户名是否存在
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, req.getUsername()));
        if (count > 0) {
            return Result.error("用户名已存在");
        }

        // 2. 创建商家用户
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNickname(StringUtils.hasText(req.getStoreName()) ? req.getStoreName() : "新商家");
        user.setRole("MERCHANT");
        user.setStatus(1);

        userMapper.insert(user);

        // 3. 回填 MerchantId (商家自营模式)
        user.setMerchantId(user.getId());
        userMapper.updateById(user);

        return Result.success("商家账号创建成功");
    }

    @GetMapping("/list")
    public Result<Page<User>> list(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer size) {
        Page<User> pageParam = new Page<>(page, size);
        Page<User> result = userMapper.selectPage(pageParam, new LambdaQueryWrapper<User>()
                .eq(User::getRole, "MERCHANT")
                .orderByDesc(User::getCreateTime));

        result.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(result);
    }

    @Data
    static class MerchantCreateReq {
        private String username;
        private String password;
        private String storeName;
    }
}