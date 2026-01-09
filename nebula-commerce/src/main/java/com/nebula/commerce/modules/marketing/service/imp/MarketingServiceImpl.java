package com.nebula.commerce.modules.marketing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.commerce.infrastructure.web.Result;
import com.nebula.commerce.infrastructure.web.SecurityUtils;
import com.nebula.commerce.modules.marketing.entity.Coupon;
import com.nebula.commerce.modules.marketing.entity.Seckill;
import com.nebula.commerce.modules.marketing.entity.UserCoupon;
import com.nebula.commerce.modules.marketing.mapper.CouponMapper;
import com.nebula.commerce.modules.marketing.mapper.SeckillMapper;
import com.nebula.commerce.modules.marketing.mapper.UserCouponMapper;
import com.nebula.commerce.modules.marketing.service.MarketingService;
import com.nebula.commerce.modules.product.entity.Product;
import com.nebula.commerce.modules.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketingServiceImpl implements MarketingService {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;
    private final SeckillMapper seckillMapper;
    private final ProductService productService;
    private final SecurityUtils securityUtils; // [恢复] 使用统一工具类

    // --- 优惠券逻辑 ---

    @Override
    public boolean createCoupon(Coupon coupon) {
        Long merchantId = securityUtils.getCurrentUserMerchantId();
        // 如果是管理员(merchantId为null), 默认为平台券(0)
        coupon.setMerchantId(merchantId == null ? 0L : merchantId);

        coupon.setReceiveCount(0);
        if (coupon.getStatus() == null) coupon.setStatus(1);
        return couponMapper.insert(coupon) > 0;
    }

    @Override
    public Page<Coupon> listCouponsAdmin(Integer page, Integer size) {
        // DataScopeHandler 会自动拦截 SQL 处理 merchant_id 过滤
        return couponMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Coupon>().orderByDesc(Coupon::getCreateTime));
    }

    @Override
    public boolean updateCouponStatus(Long id, Integer status) {
        // 更新时 DataScope 也会生效，防止商家操作别人的券
        Coupon update = new Coupon();
        update.setId(id);
        update.setStatus(status);
        return couponMapper.updateById(update) > 0;
    }

    @Override
    public boolean deleteCoupon(Long id) {
        return couponMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> receiveCoupon(Long userId, Long couponId) {
        // 1. 基础检查
        Coupon coupon = couponMapper.selectById(couponId);
        LocalDateTime now = LocalDateTime.now();
        if (coupon == null || coupon.getStatus() != 1) {
            return Result.error("优惠券已下架");
        }
        if (now.isBefore(coupon.getStartTime())) {
            return Result.error("活动尚未开始");
        }
        if (now.isAfter(coupon.getEndTime())) {
            return Result.error("活动已结束");
        }

        // 2. 检查个人限领 (读操作，不需要锁)
        Long hasCount = userCouponMapper.selectCount(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getCouponId, couponId));
        if (hasCount >= coupon.getPerLimit()) {
            return Result.error("您已达到领取上限");
        }

        // 3. [核心修复] 原子性扣减库存 (防止超发)
        // SQL: UPDATE mkt_coupon SET receive_count = receive_count + 1 WHERE id = ? AND receive_count < publish_count
        boolean updateSuccess = couponMapper.update(null, new LambdaUpdateWrapper<Coupon>()
                .setSql("receive_count = receive_count + 1")
                .eq(Coupon::getId, couponId)
                .lt(Coupon::getReceiveCount, coupon.getPublishCount()) // 乐观锁条件
        ) > 0;

        if (!updateSuccess) {
            return Result.error("优惠券已抢光");
        }

        // 4. 发券
        UserCoupon uc = new UserCoupon();
        uc.setUserId(userId);
        uc.setCouponId(couponId);
        uc.setStatus(0); // 0:未使用
        userCouponMapper.insert(uc);

        return Result.success("领取成功");
    }

    @Override
    public List<UserCoupon> getMyCoupons(Long userId) {
        List<UserCoupon> list = userCouponMapper.selectList(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .orderByDesc(UserCoupon::getCreateTime));

        if (list.isEmpty()) return list;

        fillCouponDetails(list);
        return list;
    }

    @Override
    public Page<Coupon> getAvailableCoupons(Integer page, Integer size) {
        LocalDateTime now = LocalDateTime.now();
        return couponMapper.selectPage(new Page<>(page, size), new LambdaQueryWrapper<Coupon>()
                .eq(Coupon::getStatus, 1)
                .le(Coupon::getStartTime, now)
                .ge(Coupon::getEndTime, now)
                .apply("receive_count < publish_count") // 仅查询有库存的
                .orderByDesc(Coupon::getCreateTime));
    }

    @Override
    public List<UserCoupon> getUsableCoupons(Long userId, BigDecimal orderAmount) {
        // 1. 查出用户所有未使用的券
        List<UserCoupon> list = userCouponMapper.selectList(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getStatus, 0)); // 0:未使用

        if (list.isEmpty()) return new ArrayList<>();

        // 2. 填充详情 (面额、门槛、时间)
        fillCouponDetails(list);

        // 3. 内存筛选
        LocalDateTime now = LocalDateTime.now();
        return list.stream().filter(uc -> {
            // 排除数据异常
            if (uc.getMinPoint() == null || uc.getEndTime() == null) return false;
            // 排除过期
            if (now.isAfter(uc.getEndTime())) return false;
            if (now.isBefore(uc.getStartTime())) return false;
            // 排除金额不满足
            if (orderAmount.compareTo(uc.getMinPoint()) < 0) return false;

            return true;
        }).collect(Collectors.toList());
    }

    /**
     * 辅助方法：批量填充优惠券详情 (名称、面额、有效期)
     */
    private void fillCouponDetails(List<UserCoupon> userCoupons) {
        List<Long> couponIds = userCoupons.stream().map(UserCoupon::getCouponId).collect(Collectors.toList());
        if (couponIds.isEmpty()) return;

        List<Coupon> coupons = couponMapper.selectBatchIds(couponIds);
        Map<Long, Coupon> couponMap = coupons.stream().collect(Collectors.toMap(Coupon::getId, Function.identity()));

        for (UserCoupon uc : userCoupons) {
            Coupon c = couponMap.get(uc.getCouponId());
            if (c != null) {
                uc.setName(c.getName());
                uc.setAmount(c.getAmount());
                uc.setMinPoint(c.getMinPoint());
                uc.setStartTime(c.getStartTime());
                uc.setEndTime(c.getEndTime());
                uc.setMerchantId(c.getMerchantId());
            }
        }
    }

    // --- 秒杀逻辑 ---

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createSeckill(Seckill seckill) {
        Long merchantId = securityUtils.getCurrentUserMerchantId();

        Product product = productService.getById(seckill.getProductId());
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }

        // 权限校验
        if (merchantId != null) {
            if (!merchantId.equals(product.getMerchantId())) {
                throw new RuntimeException("无权使用非本店商品创建活动");
            }
            seckill.setMerchantId(merchantId);
        } else {
            // 管理员创建，继承商品的 merchantId
            seckill.setMerchantId(product.getMerchantId() != null ? product.getMerchantId() : 0L);
        }

        // [新增] 库存逻辑校验
        if (seckill.getStockCount() > product.getStock()) {
            throw new RuntimeException("秒杀库存不能超过商品当前总库存 (" + product.getStock() + ")");
        }

        // 时间冲突校验
        Long overlapCount = seckillMapper.selectCount(new LambdaQueryWrapper<Seckill>()
                .eq(Seckill::getProductId, seckill.getProductId())
                .eq(Seckill::getStatus, 1)
                .and(wrapper -> wrapper
                        .le(Seckill::getStartTime, seckill.getEndTime())
                        .ge(Seckill::getEndTime, seckill.getStartTime())
                ));
        if (overlapCount > 0) {
            throw new IllegalArgumentException("该商品在当前时间段内已有秒杀活动");
        }

        // 快照信息
        seckill.setProductName(product.getName());
        seckill.setMainImage(product.getMainImage());
        seckill.setOriginalPrice(product.getPrice());

        if (seckill.getStatus() == null) seckill.setStatus(1);

        return seckillMapper.insert(seckill) > 0;
    }

    @Override
    public Page<Seckill> listSeckillsAdmin(Integer page, Integer size) {
        // Admin/Merchant 看到的数据范围由 DataScopeHandler 控制
        return seckillMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Seckill>().orderByDesc(Seckill::getStartTime));
    }

    @Override
    public List<Seckill> getCurrentSeckills() {
        LocalDateTime now = LocalDateTime.now();
        // 查询正在进行或即将开始(24h内)的秒杀
        return seckillMapper.selectList(new LambdaQueryWrapper<Seckill>()
                .eq(Seckill::getStatus, 1)
                .ge(Seckill::getEndTime, now) // 还没结束
                .le(Seckill::getStartTime, now.plusHours(24)) // 24小时内开始
                .orderByAsc(Seckill::getStartTime));
    }
}