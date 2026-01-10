package com.nebula.commerce.modules.marketing.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.commerce.infrastructure.web.Result;
import com.nebula.commerce.infrastructure.web.SecurityUtils;
import com.nebula.commerce.modules.marketing.entity.Coupon;
import com.nebula.commerce.modules.marketing.entity.Seckill;
import com.nebula.commerce.modules.marketing.entity.UserCoupon;
import com.nebula.commerce.modules.marketing.service.MarketingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/marketing")
@RequiredArgsConstructor
public class MarketingController {

    private final MarketingService marketingService;
    private final SecurityUtils securityUtils;

    // --- 优惠券接口 ---

    @PostMapping("/admin/coupon/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'MERCHANT')")
    public Result<String> createCoupon(@RequestBody @Valid Coupon coupon) {
        marketingService.createCoupon(coupon);
        return Result.success("优惠券创建成功");
    }

    @GetMapping("/admin/coupon/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'MERCHANT')")
    public Result<Page<Coupon>> listCoupons(@RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(marketingService.listCouponsAdmin(page, size));
    }

    @PostMapping("/admin/coupon/status/{id}/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MERCHANT')")
    public Result<String> updateCouponStatus(@PathVariable Long id, @PathVariable Integer status) {
        marketingService.updateCouponStatus(id, status);
        return Result.success("状态更新成功");
    }

    @DeleteMapping("/admin/coupon/delete/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MERCHANT')")
    public Result<String> deleteCoupon(@PathVariable Long id) {
        marketingService.deleteCoupon(id);
        return Result.success("删除成功");
    }

    // --- 用户端优惠券接口 ---

    @GetMapping("/coupon/center")
    public Result<Page<Coupon>> couponCenter(@RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(marketingService.getAvailableCoupons(page, size));
    }

    @PostMapping("/coupon/receive/{id}")
    public Result<String> receiveCoupon(@PathVariable Long id) {
        return marketingService.receiveCoupon(securityUtils.getCurrentUserId(), id);
    }

    @GetMapping("/coupon/my")
    public Result<List<UserCoupon>> myCoupons() {
        return Result.success(marketingService.getMyCoupons(securityUtils.getCurrentUserId()));
    }

    @GetMapping("/coupon/usable")
    public Result<List<UserCoupon>> usableCoupons(@RequestParam BigDecimal orderAmount) {
        return Result.success(marketingService.getUsableCoupons(securityUtils.getCurrentUserId(), orderAmount));
    }

    // --- 秒杀接口 ---

    @PostMapping("/admin/seckill/create")
    @PreAuthorize("hasAnyRole('ADMIN', 'MERCHANT')")
    public Result<String> createSeckill(@RequestBody @Valid Seckill seckill) {
        marketingService.createSeckill(seckill);
        return Result.success("秒杀活动发布成功");
    }

    @GetMapping("/admin/seckill/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'MERCHANT')")
    public Result<Page<Seckill>> listSeckills(@RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(marketingService.listSeckillsAdmin(page, size));
    }

    @GetMapping("/portal/seckill/list")
    public Result<List<Seckill>> listCurrentSeckills() {
        return Result.success(marketingService.getCurrentSeckills());
    }

    @GetMapping("/seckill/current")
    public Result<List<Seckill>> listCurrentSeckillsCompat() {
        return Result.success(marketingService.getCurrentSeckills());
    }
}
