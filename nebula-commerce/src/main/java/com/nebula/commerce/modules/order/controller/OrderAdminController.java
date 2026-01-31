package com.nebula.commerce.modules.order.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.commerce.infrastructure.web.Result;
import com.nebula.commerce.infrastructure.web.SecurityUtils;
import com.nebula.commerce.modules.member.entity.User;
import com.nebula.commerce.modules.member.mapper.UserMapper;
import com.nebula.commerce.modules.order.dto.OrderDeliverReq;
import com.nebula.commerce.modules.order.entity.Order;
import com.nebula.commerce.modules.order.entity.OrderItem;
import com.nebula.commerce.modules.order.mapper.OrderItemMapper;
import com.nebula.commerce.modules.order.mapper.OrderMapper;
import com.nebula.commerce.modules.order.service.OrderService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/order")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MERCHANT')")
public class OrderAdminController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final SecurityUtils securityUtils;
    private final UserMapper userMapper;

    @Data
    static class OrderAuditReq {
        private String orderNo;
        private Boolean pass;
        private String remark;
    }

    @GetMapping("/list")
    public Result<Page<Order>> list(@RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size,
                                    @RequestParam(required = false) String orderNo,
                                    @RequestParam(required = false) Integer status) {
        Page<Order> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        Long merchantId = getMerchantIdFilter();
        if (merchantId != null) {
            wrapper.eq(Order::getMerchantId, merchantId);
        }

        if (StringUtils.isNotBlank(orderNo)) {
            wrapper.eq(Order::getOrderNo, orderNo);
        }
        if (status != null) {
            if (status == 5) {
                wrapper.in(Order::getStatus, 5, 6);
            } else {
                wrapper.eq(Order::getStatus, status);
            }
        }
        wrapper.orderByDesc(Order::getCreateTime);
        return Result.success(orderMapper.selectPage(pageParam, wrapper));
    }

    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) return Result.error("订单不存在");

        checkPermission(order);

        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id)
        );

        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        data.put("items", items);
        return Result.success(data);
    }

    @PostMapping("/deliver")
    public Result<String> deliver(@RequestBody OrderDeliverReq req) {
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, req.getOrderNo()));
        if (order == null) return Result.error("订单不存在");
        checkPermission(order);

        orderService.shipOrder(req);
        return Result.success("发货成功");
    }

    @PostMapping("/audit")
    public Result<String> audit(@RequestBody OrderAuditReq req) {
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, req.getOrderNo()));
        if (order == null) return Result.error("订单不存在");
        checkPermission(order);

        orderService.auditRefund(req.getOrderNo(), req.getPass(), req.getRemark());
        return Result.success(req.getPass() ? "已同意退款" : "已拒绝退款申请");
    }

    // 导出功能保持不变 (View Only)
    @GetMapping("/export")
    public void export(HttpServletResponse response,
                       @RequestParam(required = false) String orderNo,
                       @RequestParam(required = false) Integer status) {

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();

        Long merchantId = getMerchantIdFilter();
        if (merchantId != null) {
            wrapper.eq(Order::getMerchantId, merchantId);
        }

        if (StringUtils.isNotBlank(orderNo)) {
            wrapper.eq(Order::getOrderNo, orderNo);
        }
        if (status != null) {
            if (status == 5) {
                wrapper.in(Order::getStatus, 5, 6);
            } else {
                wrapper.eq(Order::getStatus, status);
            }
        }
        wrapper.orderByDesc(Order::getCreateTime);

        List<Order> orders = orderMapper.selectList(wrapper);

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Order order : orders) {
            checkPermission(order);

            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId())
            );
            if (items == null || items.isEmpty()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("订单号", order.getOrderNo());
                row.put("交易号", order.getTradeNo());
                row.put("商家ID", order.getMerchantId());
                row.put("用户ID", order.getUserId());
                row.put("状态", order.getStatus());
                row.put("实付金额", order.getTotalAmount());
                row.put("收货人", order.getReceiverName());
                row.put("手机号", order.getReceiverPhone());
                row.put("地址", order.getReceiverAddress());
                row.put("商品ID", null);
                row.put("商品名", null);
                row.put("单价", null);
                row.put("数量", null);
                row.put("小计", null);
                row.put("创建时间", order.getCreateTime());
                rows.add(row);
                continue;
            }

            for (OrderItem item : items) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("订单号", order.getOrderNo());
                row.put("交易号", order.getTradeNo());
                row.put("商家ID", order.getMerchantId());
                row.put("用户ID", order.getUserId());
                row.put("状态", order.getStatus());
                row.put("实付金额", order.getTotalAmount());
                row.put("收货人", order.getReceiverName());
                row.put("手机号", order.getReceiverPhone());
                row.put("地址", order.getReceiverAddress());
                row.put("商品ID", item.getProductId());
                row.put("商品名", item.getProductName());
                row.put("单价", item.getCurrentUnitPrice());
                row.put("数量", item.getQuantity());
                row.put("小计", item.getTotalPrice());
                row.put("创建时间", order.getCreateTime());
                rows.add(row);
            }
        }

        String fileName = "orders_" + DateUtil.format(DateUtil.date(), "yyyyMMddHHmmss") + ".xlsx";
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            response.setHeader(
                    "Content-Disposition",
                    "attachment;filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8)
            );

            try (ExcelWriter writer = ExcelUtil.getWriter(true);
                 ServletOutputStream out = response.getOutputStream()) {
                writer.write(rows, true);
                writer.flush(out, true);
            }
        } catch (IOException e) {
            throw new RuntimeException("导出失败: " + e.getMessage(), e);
        }
    }

    private Long getMerchantIdFilter() {
        Long userId = securityUtils.getCurrentUserId();
        User user = userMapper.selectById(userId);
        if ("MERCHANT".equals(user.getRole())) {
            return user.getMerchantId() == null ? -1L : user.getMerchantId();
        }
        return null;
    }

    private void checkPermission(Order order) {
        Long filterId = getMerchantIdFilter();
        if (filterId != null) {
            if (!filterId.equals(order.getMerchantId())) {
                throw new RuntimeException("无权操作非本店订单");
            }
        }
    }
}
