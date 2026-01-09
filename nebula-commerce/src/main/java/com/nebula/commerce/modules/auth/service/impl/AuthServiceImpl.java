package com.nebula.commerce.modules.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nebula.commerce.infrastructure.web.JwtUtils;
import com.nebula.commerce.infrastructure.web.SecurityUtils;
import com.nebula.commerce.modules.auth.dto.LoginRequest;
import com.nebula.commerce.modules.auth.dto.PasswordUpdateReq;
import com.nebula.commerce.modules.auth.dto.RegisterRequest;
import com.nebula.commerce.modules.auth.service.AuthService;
import com.nebula.commerce.modules.member.entity.User;
import com.nebula.commerce.modules.member.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final SecurityUtils securityUtils;

    @Value("${app.admin-invite-code:Nebula2026}")
    private String adminInviteCode;

    @Override
    public Map<String, Object> login(LoginRequest request) {
        // 1. 基础参数校验
        if (!StringUtils.hasText(request.getLoginType())) {
            throw new IllegalArgumentException("非法登录：未指定登录类型");
        }

        // 2. 查询用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("登录失败：用户名或密码错误 | User: {}", request.getUsername());
            throw new IllegalArgumentException("账号或密码错误");
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            log.warn("登录阻止：账号已禁用 | User: {}", request.getUsername());
            throw new RuntimeException("账号已被禁用，请联系管理员");
        }

        // 3. 权限物理隔离校验
        String role = user.getRole();
        String loginType = request.getLoginType();

        validateLoginPermission(loginType, role);

        // 4. 生成 Token
        String token = jwtUtils.generateToken(user.getUsername());

        // 5. 组装返回结果 (避免直接修改 Entity 导致缓存污染)
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("role", user.getRole());

        // 脱敏处理：构造一个新的 Map 或对象返回，而不是修改 user 实体
        Map<String, Object> userVo = new HashMap<>();
        userVo.put("id", user.getId());
        userVo.put("username", user.getUsername());
        userVo.put("nickname", user.getNickname());
        userVo.put("avatar", user.getAvatar());
        userVo.put("merchantId", user.getMerchantId());
        data.put("user", userVo);

        log.info("用户登录成功 | User: {} | Role: {} | Type: {}", user.getUsername(), role, loginType);
        return data;
    }

    private void validateLoginPermission(String loginType, String role) {
        if ("store".equals(loginType)) {
            // 商城入口：只允许普通用户
            if (!"USER".equals(role)) {
                throw new RuntimeException("管理员/商家账号禁止登录商城前台");
            }
        } else if ("admin".equals(loginType)) {
            // 后台入口：只允许管理员和商家
            if ("USER".equals(role)) {
                throw new RuntimeException("普通用户无权访问后台管理系统");
            }
        } else {
            throw new IllegalArgumentException("不支持的登录类型");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerUser(RegisterRequest req) {
        if (checkUsernameExists(req.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNickname(StringUtils.hasText(req.getNickname()) ? req.getNickname() : "用户" + req.getUsername());
        user.setRole("USER");
        user.setStatus(1);
        userMapper.insert(user);
        log.info("新用户注册: {}", user.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerMerchant(RegisterRequest req) {
        if (checkUsernameExists(req.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNickname(StringUtils.hasText(req.getStoreName()) ? req.getStoreName() : "未命名店铺");
        user.setRole("MERCHANT");
        user.setStatus(1);

        // 1. 先插入获取 ID
        userMapper.insert(user);

        // 2. 回填 MerchantId (商家自营模式)
        user.setMerchantId(user.getId());
        userMapper.updateById(user);

        log.info("新商家入驻: {} | ID: {}", user.getUsername(), user.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerAdmin(RegisterRequest req) {
        if (!adminInviteCode.equals(req.getInviteCode())) {
            log.warn("管理员注册失败：邀请码错误 | User: {}", req.getUsername());
            throw new RuntimeException("无效的管理员邀请码");
        }
        if (checkUsernameExists(req.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNickname("管理员");
        user.setRole("ADMIN");
        user.setStatus(1);
        userMapper.insert(user);
        log.info("管理员注册成功: {}", user.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(PasswordUpdateReq req) {
        // 使用安全工具类获取当前用户，确保操作的是自己
        User user = securityUtils.getCurrentUser();
        if (user == null) {
            throw new RuntimeException("用户未登录或不存在");
        }

        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userMapper.updateById(user);
        log.info("用户修改密码: {}", user.getUsername());
    }

    private boolean checkUsernameExists(String username) {
        return userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)) > 0;
    }
}