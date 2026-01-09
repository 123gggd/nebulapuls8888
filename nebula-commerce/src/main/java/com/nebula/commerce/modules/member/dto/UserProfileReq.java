package com.nebula.commerce.modules.member.dto;

import lombok.Data;

@Data
public class UserProfileReq {
    private String nickname;
    private String avatar;
    // 后续可扩展性别、生日等字段
}