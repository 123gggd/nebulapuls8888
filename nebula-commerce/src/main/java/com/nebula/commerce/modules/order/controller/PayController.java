package com.nebula.commerce.modules.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nebula.commerce.infrastructure.web.SecurityUtils;
import com.nebula.commerce.infrastructure.web.Result;
import com.nebula.commerce.modules.order.dto.TradePayDetailVo;
import com.nebula.commerce.modules.order.entity.Order;
import com.nebula.commerce.modules.order.mapper.OrderMapper;
import com.nebula.commerce.modules.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
public class PayController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final SecurityUtils securityUtils;

    @GetMapping("/trade/{tradeNo}")
    public Result<TradePayDetailVo> getTradePayDetail(@PathVariable String tradeNo) {
        Long userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未授权：请先登录");
        }

        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getTradeNo, tradeNo)
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreateTime));

        if (orders == null || orders.isEmpty()) {
            return Result.error("交易单不存在");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        boolean hasUnpaid = false;
        for (Order order : orders) {
            if (order.getTotalAmount() != null) {
                totalAmount = totalAmount.add(order.getTotalAmount());
            }
            if (order.getStatus() != null && order.getStatus() == 0) {
                hasUnpaid = true;
            }
        }

        TradePayDetailVo vo = new TradePayDetailVo();
        vo.setTradeNo(tradeNo);
        vo.setTotalAmount(totalAmount);
        vo.setPaid(!hasUnpaid);
        vo.setPayable(hasUnpaid);
        vo.setOrders(orders);
        return Result.success(vo);
    }

    /**
     * 聚合支付接口 (TradeNo)
     */
    @PostMapping("/trade/{tradeNo}")
    public Result<String> payByTradeNo(@PathVariable String tradeNo) {
        orderService.payTrade(tradeNo);
        return Result.success("支付成功");
    }

    /**
     * 单个订单支付
     */
    @PostMapping("/order/{orderNo}")
    public Result<String> payByOrderNo(@PathVariable String orderNo) {
        orderService.paySingleOrder(orderNo);
        return Result.success("支付成功");
    }
}