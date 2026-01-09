package com.nebula.commerce.modules.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.commerce.infrastructure.web.Result;
import com.nebula.commerce.infrastructure.web.SecurityUtils;
import com.nebula.commerce.modules.member.entity.User;
import com.nebula.commerce.modules.member.mapper.UserMapper;
import com.nebula.commerce.modules.product.entity.Review;
import com.nebula.commerce.modules.product.mapper.ReviewMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 后台评价管理接口 (支持商家回复)
 */
@RestController
@RequestMapping("/api/admin/review")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MERCHANT')")
public class ReviewAdminController {

    private final ReviewMapper reviewMapper;
    private final SecurityUtils securityUtils;
    private final UserMapper userMapper;

    /**
     * 评价列表
     * 自动数据隔离：Merchant登录只能看到 merchant_id = 自己的评价
     */
    @GetMapping("/list")
    public Result<Page<Review>> list(@RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "10") Integer size,
                                     @RequestParam(required = false) String productName,
                                     @RequestParam(required = false) Integer status) {
        Page<Review> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();

        // 显式追加商家过滤 (双重保险，防止 DataScope 失效)
        Long currentMerchantId = getMerchantId();
        if (currentMerchantId != null) {
            wrapper.eq(Review::getMerchantId, currentMerchantId);
        }

        if (StringUtils.isNotBlank(productName)) {
            wrapper.like(Review::getProductName, productName);
        }
        if (status != null) {
            wrapper.eq(Review::getStatus, status);
        }
        wrapper.orderByDesc(Review::getCreateTime);

        return Result.success(reviewMapper.selectPage(pageParam, wrapper));
    }

    /**
     * 商家回复评价
     */
    @PostMapping("/reply")
    public Result<String> reply(@RequestBody ReplyReq req) {
        Review review = reviewMapper.selectById(req.getId());
        if (review == null) {
            return Result.error("评价不存在");
        }

        // 权限校验：只能回复自己店铺商品的评价
        Long currentMerchantId = getMerchantId();
        if (currentMerchantId != null && !currentMerchantId.equals(review.getMerchantId())) {
            return Result.error("无权操作非本店评价");
        }

        review.setReplyContent(req.getContent());
        review.setReplyTime(LocalDateTime.now());
        reviewMapper.updateById(review);

        return Result.success("回复成功");
    }

    /**
     * 审核/隐藏评价
     */
    @PostMapping("/status/{id}/{status}")
    public Result<String> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        Review review = reviewMapper.selectById(id);
        if (review == null) return Result.error("评价不存在");

        Long currentMerchantId = getMerchantId();
        if (currentMerchantId != null && !currentMerchantId.equals(review.getMerchantId())) {
            return Result.error("无权操作");
        }

        review.setStatus(status);
        reviewMapper.updateById(review);
        return Result.success("状态更新成功");
    }

    private Long getMerchantId() {
        Long userId = securityUtils.getCurrentUserId();
        User user = userMapper.selectById(userId);
        if ("MERCHANT".equals(user.getRole())) {
            return user.getMerchantId();
        }
        return null;
    }

    @Data
    static class ReplyReq {
        private Long id;
        private String content;
    }
}