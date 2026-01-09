package com.nebula.commerce.modules.marketing.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("mkt_user_coupon")
public class UserCoupon {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long couponId;
    private Integer status; // 0:未使用 1:已使用 2:已过期
    private Long orderId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime useTime;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;

    // --- 扩展字段 (用于前端展示) ---
    @TableField(exist = false)
    private String name;

    @TableField(exist = false)
    private BigDecimal amount;

    @TableField(exist = false)
    private BigDecimal minPoint;

    @TableField(exist = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime startTime;

    @TableField(exist = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime endTime;

    /**
     * [修复] 新增商户ID字段，用于前端区分是平台券还是店铺券
     */
    @TableField(exist = false)
    private Long merchantId;
}