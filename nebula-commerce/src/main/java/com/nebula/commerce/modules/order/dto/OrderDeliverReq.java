package com.nebula.commerce.modules.order.dto;

import lombok.Data;

@Data
public class OrderDeliverReq {
    private String orderNo;
    private String logisticsCompany; // 物流公司 (e.g., 顺丰速运)
    private String trackingNo;       // 运单号
}