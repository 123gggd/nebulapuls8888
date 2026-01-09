package com.nebula.commerce.modules.member.controller;

import com.nebula.commerce.infrastructure.web.Result;
import com.nebula.commerce.infrastructure.web.SecurityUtils;
import com.nebula.commerce.modules.auth.dto.PasswordUpdateReq;
import com.nebula.commerce.modules.member.dto.UserProfileReq;
import com.nebula.commerce.modules.member.entity.User;
import com.nebula.commerce.modules.member.mapper.UserMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    @GetMapping("/info")
    public Result<User> info() {
        Long userId = securityUtils.getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPassword(null); // 脱敏
        }
        return Result.success(user);
    }

    /**
     * 更新个人资料
     */
    @PostMapping("/update")
    public Result<String> update(@RequestBody UserProfileReq req) {
        Long userId = securityUtils.getCurrentUserId();

        User update = new User();
        update.setId(userId);
        if (StringUtils.hasText(req.getNickname())) {
            update.setNickname(req.getNickname());
        }
        if (StringUtils.hasText(req.getAvatar())) {
            update.setAvatar(req.getAvatar());
        }

        userMapper.updateById(update);
        return Result.success("资料更新成功");
    }

    /**
     * 修改密码
     * 复用 Auth 模块的 PasswordUpdateReq DTO
     */
    @PostMapping("/password")
    public Result<String> updatePassword(@RequestBody @Valid PasswordUpdateReq req) {
        Long userId = securityUtils.getCurrentUserId();
        User user = userMapper.selectById(userId);

        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            return Result.error("原密码错误");
        }

        User update = new User();
        update.setId(userId);
        update.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userMapper.updateById(update);

        return Result.success("密码修改成功");
    }
}