package com.nebula.commerce.modules.marketing.service;

import com.nebula.commerce.infrastructure.web.Result;
import com.nebula.commerce.infrastructure.web.SecurityUtils;
import com.nebula.commerce.modules.marketing.entity.Coupon;
import com.nebula.commerce.modules.marketing.mapper.CouponMapper;
import com.nebula.commerce.modules.marketing.mapper.SeckillMapper;
import com.nebula.commerce.modules.marketing.mapper.UserCouponMapper;
import com.nebula.commerce.modules.marketing.service.impl.MarketingServiceImpl;
import com.nebula.commerce.modules.product.service.ProductService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MarketingServiceImplTest {

    @Test
    void receiveCoupon_shouldReturnError_whenOverPerLimit() {
        CouponMapper couponMapper = mock(CouponMapper.class);
        UserCouponMapper userCouponMapper = mock(UserCouponMapper.class);
        SeckillMapper seckillMapper = mock(SeckillMapper.class);
        ProductService productService = mock(ProductService.class);
        SecurityUtils securityUtils = mock(SecurityUtils.class);

        MarketingServiceImpl service = new MarketingServiceImpl(couponMapper, userCouponMapper, seckillMapper, productService, securityUtils);

        Coupon coupon = new Coupon();
        coupon.setId(1L);
        coupon.setStatus(1);
        coupon.setStartTime(LocalDateTime.now().minusDays(1));
        coupon.setEndTime(LocalDateTime.now().plusDays(1));
        coupon.setPerLimit(1);
        coupon.setPublishCount(10);
        coupon.setReceiveCount(0);

        when(couponMapper.selectById(1L)).thenReturn(coupon);
        when(userCouponMapper.selectCount(any())).thenReturn(1L);

        Result<String> res = service.receiveCoupon(100L, 1L);
        assertEquals(500, res.getCode());
        assertEquals("您已达到领取上限", res.getMessage());

        verify(couponMapper, never()).update(any(), any());
        verify(userCouponMapper, never()).insert(any());
    }

    @Test
    void receiveCoupon_shouldReturnError_whenSoldOut() {
        CouponMapper couponMapper = mock(CouponMapper.class);
        UserCouponMapper userCouponMapper = mock(UserCouponMapper.class);
        SeckillMapper seckillMapper = mock(SeckillMapper.class);
        ProductService productService = mock(ProductService.class);
        SecurityUtils securityUtils = mock(SecurityUtils.class);

        MarketingServiceImpl service = new MarketingServiceImpl(couponMapper, userCouponMapper, seckillMapper, productService, securityUtils);

        Coupon coupon = new Coupon();
        coupon.setId(1L);
        coupon.setStatus(1);
        coupon.setStartTime(LocalDateTime.now().minusDays(1));
        coupon.setEndTime(LocalDateTime.now().plusDays(1));
        coupon.setPerLimit(1);
        coupon.setPublishCount(10);
        coupon.setReceiveCount(10);

        when(couponMapper.selectById(1L)).thenReturn(coupon);
        when(userCouponMapper.selectCount(any())).thenReturn(0L);
        when(couponMapper.update(isNull(), any())).thenReturn(0);

        Result<String> res = service.receiveCoupon(100L, 1L);
        assertEquals(500, res.getCode());
        assertEquals("优惠券已抢光", res.getMessage());

        verify(userCouponMapper, never()).insert(any());
    }

    @Test
    void receiveCoupon_shouldSuccess_whenInStock() {
        CouponMapper couponMapper = mock(CouponMapper.class);
        UserCouponMapper userCouponMapper = mock(UserCouponMapper.class);
        SeckillMapper seckillMapper = mock(SeckillMapper.class);
        ProductService productService = mock(ProductService.class);
        SecurityUtils securityUtils = mock(SecurityUtils.class);

        MarketingServiceImpl service = new MarketingServiceImpl(couponMapper, userCouponMapper, seckillMapper, productService, securityUtils);

        Coupon coupon = new Coupon();
        coupon.setId(1L);
        coupon.setStatus(1);
        coupon.setStartTime(LocalDateTime.now().minusDays(1));
        coupon.setEndTime(LocalDateTime.now().plusDays(1));
        coupon.setPerLimit(1);
        coupon.setPublishCount(10);
        coupon.setReceiveCount(0);

        when(couponMapper.selectById(1L)).thenReturn(coupon);
        when(userCouponMapper.selectCount(any())).thenReturn(0L);
        when(couponMapper.update(isNull(), any())).thenReturn(1);
        when(userCouponMapper.insert(any())).thenReturn(1);

        Result<String> res = service.receiveCoupon(100L, 1L);
        assertEquals(200, res.getCode());
        assertEquals("操作成功", res.getMessage());
        assertEquals("领取成功", res.getData());

        verify(userCouponMapper, times(1)).insert(any());
    }
}
