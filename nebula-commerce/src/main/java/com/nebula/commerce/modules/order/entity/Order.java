package com.nebula.commerce.modules.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sys_order")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;

    // 子订单号 (展示给商家的订单号)
    private String orderNo;

    /**
     * [Phase 2] 支付交易号
     * 用于关联多个子订单。用户支付时，生成一个 tradeNo，支付该号下的所有 orderNo。
     * 前端支付接口将改为传递 tradeNo。
     */
    private String tradeNo;

    private Long userId;

    /**
     * [Phase 2] 商家ID
     * 标记此订单属于哪个商家，用于商家后台查询和发货。
     */
    private Long merchantId;

    private BigDecimal totalAmount;

    // 0:待支付 1:待发货 2:待收货 3:已完成 4:已取消 5:售后中 6:已退款
    private Integer status;

    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;

    private String logisticsCompany;
    private String trackingNo;

    /**
     * [Phase 2] 订单备注
     * 用户下单时填写的备注信息
     */
    private String note;

    // [Phase 13] 售后信息
    private String refundReason;
    private Integer refundStatus; // 0无 1申请中 2已退款 3拒绝
    private LocalDateTime refundTime;

    private LocalDateTime paymentTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime receiveTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}