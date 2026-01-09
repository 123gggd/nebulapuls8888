package com.nebula.commerce.modules.order.dto;

import com.nebula.commerce.modules.order.entity.Order;
import com.nebula.commerce.modules.order.entity.OrderItem;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderDetailVo {
    // 订单基本信息
    private Order order;

    // 商品明细列表
    private List<OrderItem> items;

    // --- 费用明细 (计算字段) ---

    // 商品原总价 (所有商品单价*数量之和)
    private BigDecimal originalTotalAmount;

    // 优惠券抵扣金额
    private BigDecimal couponDiscountAmount;

    // 最终实付金额 (等于 order.totalAmount)
    private BigDecimal finalTotalAmount;
}