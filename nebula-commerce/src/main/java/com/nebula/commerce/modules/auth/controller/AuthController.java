package com.nebula.commerce.modules.auth.controller;

import com.nebula.commerce.infrastructure.web.Result;
import com.nebula.commerce.modules.auth.dto.LoginRequest;
import com.nebula.commerce.modules.auth.dto.PasswordUpdateReq;
import com.nebula.commerce.modules.auth.dto.RegisterRequest;
import com.nebula.commerce.modules.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody @Valid LoginRequest request) {
        Map<String, Object> data = authService.login(request);
        return Result.success(data);
    }

    @PostMapping("/register/user")
    public Result<String> registerUser(@RequestBody @Valid RegisterRequest req) {
        authService.registerUser(req);
        return Result.success("注册成功");
    }

    @PostMapping("/register/merchant")
    public Result<String> registerMerchant(@RequestBody @Valid RegisterRequest req) {
        authService.registerMerchant(req);
        return Result.success("商家入驻成功");
    }

    @PostMapping("/register/admin")
    public Result<String> registerAdmin(@RequestBody @Valid RegisterRequest req) {
        authService.registerAdmin(req);
        return Result.success("管理员注册成功");
    }

    @PostMapping("/password")
    public Result<String> updatePassword(@RequestBody @Valid PasswordUpdateReq req) {
        authService.updatePassword(req);
        return Result.success("密码修改成功，请重新登录");
    }
}