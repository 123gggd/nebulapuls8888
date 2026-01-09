package com.nebula.commerce.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 登录类型: "admin" | "store"
     */
    @NotBlank(message = "登录类型不能为空")
    private String loginType;
}