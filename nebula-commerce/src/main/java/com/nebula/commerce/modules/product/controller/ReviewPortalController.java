package com.nebula.commerce.modules.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.commerce.infrastructure.web.Result;
import com.nebula.commerce.infrastructure.web.SecurityUtils;
import com.nebula.commerce.modules.member.entity.User;
import com.nebula.commerce.modules.member.mapper.UserMapper;
import com.nebula.commerce.modules.order.entity.Order;
import com.nebula.commerce.modules.order.entity.OrderItem;
import com.nebula.commerce.modules.order.mapper.OrderItemMapper;
import com.nebula.commerce.modules.order.mapper.OrderMapper;
import com.nebula.commerce.modules.product.entity.Product;
import com.nebula.commerce.modules.product.entity.Review;
import com.nebula.commerce.modules.product.service.ProductService;
import com.nebula.commerce.modules.product.mapper.ReviewMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 前台商品评价接口
 */
@RestController
@RequestMapping("/api/portal/review")
@RequiredArgsConstructor
public class ReviewPortalController {

    private final ReviewMapper reviewMapper;
    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductService productService;
    private final SecurityUtils securityUtils;

    @GetMapping("/list")
    public Result<Page<Review>> list(@RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "10") Integer size,
                                     @RequestParam Long productId,
                                     @RequestParam(defaultValue = "all") String type) {
        Page<Review> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(Review::getProductId, productId)
                .eq(Review::getStatus, 1) // 仅显示审核通过的
                .orderByDesc(Review::getCreateTime);

        if ("pic".equals(type)) {
            wrapper.isNotNull(Review::getImages);
        }

        return Result.success(reviewMapper.selectPage(pageParam, wrapper));
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody @Valid Review review) {
        Long currentUserId = securityUtils.getCurrentUserId();
        review.setUserId(currentUserId);

        // 1. 校验订单权属
        Order order = orderMapper.selectById(review.getOrderId());
        if (order == null || !order.getUserId().equals(currentUserId)) {
            return Result.error("订单不存在或无权评价");
        }

        // 2. 防重复校验
        Long count = reviewMapper.selectCount(new LambdaQueryWrapper<Review>()
                .eq(Review::getOrderId, review.getOrderId())
                .eq(Review::getProductId, review.getProductId()));
        if (count > 0) {
            return Result.error("该商品已评价");
        }

        // 3. 【核心】回填商品快照与商家ID
        // 尝试从订单明细获取购买时的信息 (名称、图片)
        OrderItem orderItem = orderItemMapper.selectOne(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, review.getOrderId())
                .eq(OrderItem::getProductId, review.getProductId())
                .last("LIMIT 1"));

        // 查询当前商品信息 (获取 merchantId)
        Product product = productService.getById(review.getProductId());
        if (product == null) return Result.error("商品不存在");

        // 绑定商家ID (数据隔离的关键)
        review.setMerchantId(product.getMerchantId() == null ? 0L : product.getMerchantId());

        if (orderItem != null) {
            review.setProductName(orderItem.getProductName());
            review.setProductImage(orderItem.getMainImage());
            // 如果 OrderItem 有 specs 字段则回填，否则暂时留空或取 Product 默认
            // review.setSpecs(orderItem.getSpecs());
        } else {
            // 降级处理
            review.setProductName(product.getName());
            review.setProductImage(product.getMainImage());
        }

        // 4. 回填用户快照 (防用户改名/换头像后历史评价变动)
        User user = userMapper.selectById(currentUserId);
        if (user != null) {
            String showName = StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername();
            review.setUserName(showName);
            review.setUserAvatar(user.getAvatar());
        }

        // 5. 初始化状态
        review.setStatus(1);
        review.setCreateTime(LocalDateTime.now());

        reviewMapper.insert(review);
        return Result.success("评价发布成功");
    }
}