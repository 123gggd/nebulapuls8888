package com.nebula.commerce.modules.order.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nebula.commerce.modules.order.entity.Order;
import com.nebula.commerce.modules.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author HP
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTask {

    private final OrderService orderService;

    /**
     * 每分钟执行一次，关闭超过30分钟未支付的订单
     */
    @Scheduled(cron = "0 * * * * ?")
    public void closeOvertimeOrders() {
        log.info("开始扫描超时订单...");
        // 定义超时时间：30分钟前
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(30);

        // 查询：状态为0(待支付) 且 创建时间 < 30分钟前
        List<Order> overtimeOrders = orderService.list(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, 0)
                .lt(Order::getCreateTime, expireTime));

        if (overtimeOrders.isEmpty()) {
            return;
        }

        for (Order order : overtimeOrders) {
            try {
                // 调用包含库存回滚逻辑的取消方法
                orderService.cancelOrder(order.getOrderNo());
                log.info("订单 [{}] 超时自动关闭成功", order.getOrderNo());
            } catch (Exception e) {
                log.error("订单 [{}] 自动关闭失败", order.getOrderNo(), e);
            }
        }
    }
}
