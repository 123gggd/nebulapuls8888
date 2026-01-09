package com.nebula.commerce.modules.marketing.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("mkt_seckill")
public class Seckill {
    @TableId(type = IdType.AUTO)
    private Long id;

    // --- 数据隔离 ---
    private Long merchantId; // 商家ID

    @NotNull(message = "必须指定商品")
    private Long productId;

    private String productName; // 快照
    private String mainImage;   // 快照

    @NotNull(message = "秒杀价不能为空")
    private BigDecimal seckillPrice;

    private BigDecimal originalPrice;

    @NotNull(message = "秒杀库存不能为空")
    @Min(value = 1, message = "库存至少1件")
    private Integer stockCount;

    @NotNull(message = "开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    @Future(message = "结束时间必须在将来")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime endTime;

    private Integer status; // 1:上架 0:下架

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;
}