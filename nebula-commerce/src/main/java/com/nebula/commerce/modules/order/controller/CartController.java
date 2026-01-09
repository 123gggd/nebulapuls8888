package com.nebula.commerce.modules.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nebula.commerce.infrastructure.web.Result;
import com.nebula.commerce.infrastructure.web.SecurityUtils;
import com.nebula.commerce.modules.order.dto.CartItemDto;
import com.nebula.commerce.modules.order.entity.Cart;
import com.nebula.commerce.modules.order.mapper.CartMapper;
import com.nebula.commerce.modules.product.entity.Product;
import com.nebula.commerce.modules.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartMapper cartMapper;
    private final ProductService productService;
    private final SecurityUtils securityUtils;

    @GetMapping("/list")
    public Result<List<CartItemDto>> list() {
        Long userId = securityUtils.getCurrentUserId();

        List<Cart> carts = cartMapper.selectList(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId));

        if (carts.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        // [优化] 1. 提取所有商品ID
        Set<Long> productIds = carts.stream()
                .map(Cart::getProductId)
                .collect(Collectors.toSet());

        // [优化] 2. 批量查询商品信息 (解决 N+1 问题)
        List<Product> products = productService.listByIds(productIds);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // [优化] 3. 内存组装
        List<CartItemDto> dtos = new ArrayList<>();
        for (Cart cart : carts) {
            Product product = productMap.get(cart.getProductId());
            if (product != null) {
                CartItemDto dto = new CartItemDto();
                BeanUtils.copyProperties(cart, dto);

                dto.setProductName(product.getName());
                dto.setMainImage(product.getMainImage());
                dto.setPrice(product.getPrice());
                dto.setStock(product.getStock());
                // 计算小计
                dto.setTotalPrice(product.getPrice().multiply(new BigDecimal(cart.getQuantity())));

                dtos.add(dto);
            }
        }
        return Result.success(dtos);
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody Cart cart) {
        Long userId = securityUtils.getCurrentUserId();
        cart.setUserId(userId);

        Cart exist = cartMapper.selectOne(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getProductId, cart.getProductId()));

        if (exist != null) {
            exist.setQuantity(exist.getQuantity() + cart.getQuantity());
            exist.setSelected(true);
            cartMapper.updateById(exist);
        } else {
            cart.setSelected(true);
            cartMapper.insert(cart);
        }
        return Result.success("加入购物车成功");
    }

    @PostMapping("/update")
    public Result<String> update(@RequestBody Cart cart) {
        Long userId = securityUtils.getCurrentUserId();
        Cart exist = cartMapper.selectById(cart.getId());
        if (exist == null || !exist.getUserId().equals(userId)) {
            return Result.error("购物车数据不存在");
        }

        if (cart.getQuantity() != null && cart.getQuantity() > 0) {
            exist.setQuantity(cart.getQuantity());
        }
        if (cart.getSelected() != null) {
            exist.setSelected(cart.getSelected());
        }
        cartMapper.updateById(exist);
        return Result.success("更新成功");
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        Long userId = securityUtils.getCurrentUserId();
        cartMapper.delete(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getId, id)
                .eq(Cart::getUserId, userId));
        return Result.success("删除成功");
    }
}