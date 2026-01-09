package com.nebula.commerce.infrastructure.web;

import com.nebula.commerce.modules.member.entity.User;
import com.nebula.commerce.modules.member.mapper.UserMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 安全上下文工具类
 */
@Component
public class SecurityUtils {

    private final UserMapper userMapper;

    /**
     * [关键] 使用 @Lazy 注解打破循环依赖
     */
    public SecurityUtils(@Lazy UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 获取当前登录用户的 ID
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        try {
            // Spring Security 的 Principal 通常是 UserDetails 实现或字符串
            // 这里我们需要确保 UserDetails 实现中有获取 ID 的方法，或者通过 Username 查库
            // 鉴于你的 UserDetails 实现主要存了 username，这里做一个容错：
            // 如果 principal 是 UserDetails 类型，可能需要转型，但目前简单起见，
            // 我们通过用户名查库来保证获取准确 ID（虽然有一次查库开销，但对于获取 CurrentUser 来说是必要的）

            // 优化：直接调用 getCurrentUser() 查库
            User user = getCurrentUser();
            return user != null ? user.getId() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前登录用户名
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            return ((org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal()).getUsername();
        }
        if (authentication != null) {
            return authentication.getName();
        }
        return null;
    }

    /**
     * 判断当前用户是否是商家
     */
    public boolean isMerchant() {
        User user = getCurrentUser();
        return user != null && "MERCHANT".equals(user.getRole());
    }

    /**
     * 获取当前用户的 MerchantId
     */
    public Long getCurrentUserMerchantId() {
        User user = getCurrentUser();
        return user != null ? user.getMerchantId() : null;
    }

    /**
     * 从数据库获取完整用户信息
     * 依赖 UserMapper，使用了 @Lazy 注入
     */
    public User getCurrentUser() {
        String username = getCurrentUsername();
        if (username == null) return null;
        // 这里使用 selectOne 是为了保证获取到最新的数据库状态
        // 实际上可以用 Redis 缓存优化，但 MVP 阶段直连数据库更稳妥
        return userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
    }
}