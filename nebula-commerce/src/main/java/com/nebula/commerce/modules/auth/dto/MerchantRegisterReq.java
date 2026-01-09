package com.nebula.commerce.modules.auth.dto;

import lombok.Data;

@Data
public class MerchantRegisterReq {
    private String username;
    private String password;
    private String storeName; // 店铺名称
    private String contactPhone; // 联系电话
}