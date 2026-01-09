package com.nebula.commerce.modules.auth.dto;

import lombok.Data;

@Data
public class PasswordUpdateReq {
    private String oldPassword;
    private String newPassword;
}