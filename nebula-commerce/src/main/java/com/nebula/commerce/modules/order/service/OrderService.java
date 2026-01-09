package com.nebula.commerce.modules.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nebula.commerce.infrastructure.web.Result;
import com.nebula.commerce.modules.order.dto.OrderCreateReq;
import com.nebula.commerce.modules.order.dto.OrderDeliverReq;
import com.nebula.commerce.modules.order.dto.OrderDetailVo;
import com.nebula.commerce.modules.order.entity.Order;

public interface OrderService extends IService<Order> {

    // --- 核心交易流程 ---

    /**
     * 创建订单 (支持拆单)
     */
    Result<String> createOrder(Long userId, OrderCreateReq req);

    /**
     * 聚合支付成功回调
     * @param tradeNo 交易流水号
     */
    void payTrade(String tradeNo);

    /**
     * 单个订单支付成功 (兼容旧接口)
     */
    void paySingleOrder(String orderNo);

    /**
     * 商家发货
     */
    void shipOrder(OrderDeliverReq req);

    /**
     * 用户确认收货
     */
    void receiveOrder(String orderNo, Long userId);

    /**
     * 取消订单 (用户手动 或 超时自动)
     */
    void cancelOrder(String orderNo);

    // --- 查询与售后 ---

    OrderDetailVo getOrderDetailByNo(String orderNo);

    void applyRefund(String orderNo, String reason);

    void auditRefund(String orderNo, boolean pass, String remark);
}