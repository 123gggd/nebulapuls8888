package com.nebula.commerce.modules.marketing.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("mkt_coupon")
public class Coupon {
    @TableId(type = IdType.AUTO)
    private Long id;

    // --- 数据隔离核心字段 ---
    // 0或NULL: 平台全场通用券; >0: 商家专用券
    private Long merchantId;

    @NotBlank(message = "优惠券名称不能为空")
    private String name;

    @NotNull(message = "面值不能为空")
    @Min(value = 1, message = "面值至少1元")
    private BigDecimal amount;

    @NotNull(message = "使用门槛不能为空")
    @Min(value = 0, message = "门槛不能为负数")
    private BigDecimal minPoint;

    @NotNull(message = "发行量不能为空")
    @Min(value = 1, message = "发行量至少1张")
    private Integer publishCount;

    private Integer receiveCount;

    @Min(value = 1, message = "每人限领至少1张")
    private Integer perLimit;

    @NotNull(message = "开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    @Future(message = "结束时间必须在将来")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime endTime;

    private Integer status; // 1:启用 0:禁用

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;
}