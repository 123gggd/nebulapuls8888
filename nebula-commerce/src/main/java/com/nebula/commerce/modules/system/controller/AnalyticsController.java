package com.nebula.commerce.modules.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nebula.commerce.infrastructure.web.Result;
import com.nebula.commerce.infrastructure.web.SecurityUtils;
import com.nebula.commerce.modules.member.mapper.UserMapper;
import com.nebula.commerce.modules.order.entity.Order;
import com.nebula.commerce.modules.order.mapper.OrderMapper;
import com.nebula.commerce.modules.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据统计与看板接口
 * 整合了原有的 DashboardController
 */
@RestController
@RequestMapping("/api/system/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final SecurityUtils securityUtils;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'MERCHANT')")
    public Result<Map<String, Object>> dashboard() {
        Map<String, Object> data = new HashMap<>();

        // DataScopeHandler 会自动根据当前登录身份(Admin/Merchant)过滤 Order 和 Product 表

        // 1. 累计销售额 (已支付/已完成的订单)
        QueryWrapper<Order> salesWrapper = new QueryWrapper<>();
        salesWrapper.select("IFNULL(SUM(total_amount), 0)")
                .in("status", 1, 2, 3); // 1:待发货 2:已发货 3:已完成

        List<Object> salesRes = orderMapper.selectObjs(salesWrapper);
        BigDecimal totalSales = BigDecimal.ZERO;
        if (salesRes != null && !salesRes.isEmpty() && salesRes.get(0) != null) {
            totalSales = new BigDecimal(salesRes.get(0).toString());
        }
        data.put("totalSales", totalSales);

        // 2. 订单总量
        data.put("totalOrders", orderMapper.selectCount(null));

        // 3. 商品总数
        data.put("totalProducts", productMapper.selectCount(null));

        // 4. 用户总数 (仅管理员可见，商家看到0)
        if (!securityUtils.isMerchant()) {
            data.put("totalUsers", userMapper.selectCount(null));
        } else {
            data.put("totalUsers", 0);
        }

        // 5. 销售趋势图 (近7天模拟数据，真实场景需聚合查询)
        // 进阶优化：应该建立一个每日统计表(sys_daily_stats)，定时任务每天凌晨跑批
        List<String> dates = new ArrayList<>();
        List<BigDecimal> values = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            dates.add(date.toString());
            // 模拟波动
            values.add(new BigDecimal((int)(Math.random() * 2000) + 500));
        }
        data.put("trendDates", dates);
        data.put("trendValues", values);

        return Result.success(data);
    }
}