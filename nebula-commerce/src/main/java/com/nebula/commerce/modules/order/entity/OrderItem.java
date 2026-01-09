package com.nebula.commerce.modules.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sys_order_item")
public class OrderItem {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;
    private String orderNo;

    private Long productId;
    private String productName;
    private String mainImage;

    private BigDecimal currentUnitPrice; // 下单时的单价 (快照)
    private Integer quantity;
    private BigDecimal totalPrice; // 该项总价

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}