package com.nebula.commerce.modules.order.dto;

import com.nebula.commerce.modules.order.entity.Order;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TradePayDetailVo {

    private String tradeNo;

    private BigDecimal totalAmount;

    private boolean paid;

    private boolean payable;

    private List<Order> orders;
}
