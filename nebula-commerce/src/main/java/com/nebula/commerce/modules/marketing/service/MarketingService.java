package com.nebula.commerce.modules.marketing.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.commerce.infrastructure.web.Result;
import com.nebula.commerce.modules.marketing.entity.Coupon;
import com.nebula.commerce.modules.marketing.entity.Seckill;
import com.nebula.commerce.modules.marketing.entity.UserCoupon;

import java.math.BigDecimal;
import java.util.List;

public interface MarketingService {

    // --- 优惠券 ---
    boolean createCoupon(Coupon coupon);
    Page<Coupon> listCouponsAdmin(Integer page, Integer size);
    boolean updateCouponStatus(Long id, Integer status);
    boolean deleteCoupon(Long id);

    // 用户领券
    Result<String> receiveCoupon(Long userId, Long couponId);

    // 我的所有优惠券
    List<UserCoupon> getMyCoupons(Long userId);

    // 领券中心列表
    Page<Coupon> getAvailableCoupons(Integer page, Integer size);

    // [新增] 结算页获取可用优惠券 (根据订单金额筛选)
    List<UserCoupon> getUsableCoupons(Long userId, BigDecimal orderAmount);

    // --- 秒杀 ---
    boolean createSeckill(Seckill seckill);
    Page<Seckill> listSeckillsAdmin(Integer page, Integer size);
    List<Seckill> getCurrentSeckills();
}