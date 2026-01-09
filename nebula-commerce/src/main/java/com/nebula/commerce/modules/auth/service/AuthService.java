package com.nebula.commerce.modules.auth.service;

import com.nebula.commerce.modules.auth.dto.LoginRequest;
import com.nebula.commerce.modules.auth.dto.PasswordUpdateReq;
import com.nebula.commerce.modules.auth.dto.RegisterRequest;
import java.util.Map;

public interface AuthService {

    /**
     * 统一登录
     */
    Map<String, Object> login(LoginRequest request);

    /**
     * 普通用户注册
     */
    void registerUser(RegisterRequest request);

    /**
     * 商家注册
     */
    void registerMerchant(RegisterRequest request);

    /**
     * 管理员注册
     */
    void registerAdmin(RegisterRequest request);

    /**
     * 修改密码
     */
    void updatePassword(PasswordUpdateReq req);
}