package com.nebula.commerce.modules.order.controller;

import com.nebula.commerce.infrastructure.web.Result;
import com.nebula.commerce.modules.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
public class PayController {

    private final OrderService orderService;

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