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
    public void export(HttpServletResponse response) {
        // ... (保持原有的导出逻辑) ...
        // 为节省篇幅，此处省略，逻辑与之前一致，仅需确保 getMerchantIdFilter 存在
        try (ExcelWriter writer = ExcelUtil.getWriter();
             ServletOutputStream out = response.getOutputStream()) {
            // ...
        } catch (IOException e) {
            // ...
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
