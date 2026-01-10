package com.nebula.commerce.infrastructure.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nebula.commerce.modules.member.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Spring Security 认证用户详情封装
 * 修复：将引用的实体从 SysUser 更改为统一的 User (Member模块)
 */
@Data
@NoArgsConstructor
public class LoginUser implements UserDetails {

    private User user;

    public LoginUser(User user) {
        this.user = user;
    }

    /**
     * 获取商户ID (用于 DataScopeHandler 数据隔离)
     */
    public Long getMerchantId() {
        return user != null ? user.getMerchantId() : null;
    }

    /**
     * 判断是否是超级管理员
     */
    public boolean isAdmin() {
        return user != null && "ADMIN".equals(user.getRole());
    }

    /**
     * 判断是否是商家账号
     */
    public boolean isMerchant() {
        return user != null && "MERCHANT".equals(user.getRole());
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 自动添加 ROLE_ 前缀，符合 Spring Security 规范
        if (user != null && user.getRole() != null) {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
        }
        return Collections.emptyList();
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return user != null && (user.getStatus() == null || user.getStatus() == 1);
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return user != null && (user.getStatus() == null || user.getStatus() == 1);
    }
}
