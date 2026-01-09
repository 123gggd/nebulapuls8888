package com.nebula.commerce.modules.order.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartItemDto {
    private Long id; // 购物车记录ID
    private Long productId;
    private Integer quantity;
    private Boolean selected;

    // --- 商品快照信息 ---
    private String productName;
    private String mainImage;
    private BigDecimal price;      // 单价
    private BigDecimal totalPrice; // 小计 (单价 * 数量)
    private Integer stock;         // 当前库存状态
}