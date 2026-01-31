package com.nebula.commerce.modules.order.controller;

import com.nebula.commerce.infrastructure.web.Result;
import com.nebula.commerce.infrastructure.web.SecurityUtils;
import com.nebula.commerce.modules.order.dto.TradePayDetailVo;
import com.nebula.commerce.modules.order.entity.Order;
import com.nebula.commerce.modules.order.mapper.OrderMapper;
import com.nebula.commerce.modules.order.service.OrderService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PayControllerTest {

    @Test
    void getTradePayDetail_shouldReturn401_whenNotLoggedIn() {
        OrderService orderService = mock(OrderService.class);
        OrderMapper orderMapper = mock(OrderMapper.class);
        SecurityUtils securityUtils = mock(SecurityUtils.class);
        when(securityUtils.getCurrentUserId()).thenReturn(null);

        PayController controller = new PayController(orderService, orderMapper, securityUtils);
        Result<TradePayDetailVo> res = controller.getTradePayDetail("T123");

        assertEquals(401, res.getCode());
    }

    @Test
    void getTradePayDetail_shouldReturnError_whenTradeNotFound() {
        OrderService orderService = mock(OrderService.class);
        OrderMapper orderMapper = mock(OrderMapper.class);
        SecurityUtils securityUtils = mock(SecurityUtils.class);

        when(securityUtils.getCurrentUserId()).thenReturn(9L);
        when(orderMapper.selectList(any())).thenReturn(List.of());

        PayController controller = new PayController(orderService, orderMapper, securityUtils);
        Result<TradePayDetailVo> res = controller.getTradePayDetail("T123");

        assertEquals(500, res.getCode());
        assertEquals("交易单不存在", res.getMessage());
    }

    @Test
    void getTradePayDetail_shouldAggregateAmountAndPayableFlags() {
        OrderService orderService = mock(OrderService.class);
        OrderMapper orderMapper = mock(OrderMapper.class);
        SecurityUtils securityUtils = mock(SecurityUtils.class);

        when(securityUtils.getCurrentUserId()).thenReturn(9L);

        Order o1 = new Order();
        o1.setTradeNo("T123");
        o1.setUserId(9L);
        o1.setStatus(0);
        o1.setTotalAmount(new BigDecimal("10.50"));
        o1.setCreateTime(LocalDateTime.now());

        Order o2 = new Order();
        o2.setTradeNo("T123");
        o2.setUserId(9L);
        o2.setStatus(1);
        o2.setTotalAmount(new BigDecimal("20.00"));
        o2.setCreateTime(LocalDateTime.now());

        when(orderMapper.selectList(any())).thenReturn(List.of(o1, o2));

        PayController controller = new PayController(orderService, orderMapper, securityUtils);
        Result<TradePayDetailVo> res = controller.getTradePayDetail("T123");

        assertEquals(200, res.getCode());
        assertNotNull(res.getData());
        assertEquals("T123", res.getData().getTradeNo());
        assertEquals(new BigDecimal("30.50"), res.getData().getTotalAmount());
        assertTrue(res.getData().isPayable());
        assertFalse(res.getData().isPaid());
        assertEquals(2, res.getData().getOrders().size());
    }
}
