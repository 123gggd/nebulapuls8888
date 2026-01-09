package com.nebula.commerce.modules.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.commerce.infrastructure.web.Result;
import com.nebula.commerce.infrastructure.web.SecurityUtils;
import com.nebula.commerce.modules.order.dto.OrderCreateReq;
import com.nebula.commerce.modules.order.dto.OrderDetailVo;
import com.nebula.commerce.modules.order.entity.Order;
import com.nebula.commerce.modules.order.mapper.OrderMapper;
import com.nebula.commerce.modules.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final SecurityUtils securityUtils;

    @PostMapping("/create")
    public Result<String> create(@RequestBody @Valid OrderCreateReq req) {
        Long userId = securityUtils.getCurrentUserId();
        return orderService.createOrder(userId, req);
    }

    @GetMapping("/list")
    public Result<Page<Order>> myOrders(@RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "10") Integer size,
                                        @RequestParam(required = false) Integer status) {
        Long userId = securityUtils.getCurrentUserId();
        Page<Order> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        return Result.success(orderMapper.selectPage(pageParam, wrapper));
    }

    @GetMapping("/detail/{orderNo}")
    public Result<OrderDetailVo> detail(@PathVariable String orderNo) {
        // 安全校验已在 Service 内部或通过 userId 过滤
        OrderDetailVo vo = orderService.getOrderDetailByNo(orderNo);
        if (vo == null || !vo.getOrder().getUserId().equals(securityUtils.getCurrentUserId())) {
            return Result.error("订单不存在");
        }
        return Result.success(vo);
    }

    @PostMapping("/cancel/{orderNo}")
    public Result<String> cancel(@PathVariable String orderNo) {
        orderService.cancelOrder(orderNo);
        return Result.success("订单取消成功");
    }

    @PostMapping("/receive/{orderNo}")
    public Result<String> receive(@PathVariable String orderNo) {
        Long userId = securityUtils.getCurrentUserId();
        orderService.receiveOrder(orderNo, userId);
        return Result.success("确认收货成功");
    }

    @PostMapping("/pay/{orderNo}")
    public Result<String> pay(@PathVariable String orderNo) {
        orderService.paySingleOrder(orderNo);
        return Result.success("支付成功");
    }

    @PostMapping("/refund")
    public Result<String> refund(@RequestBody Map<String, String> params) {
        String orderNo = params.get("orderNo");
        String reason = params.get("reason");
        orderService.applyRefund(orderNo, reason);
        return Result.success("退款申请已提交，等待审核");
    }
}