package com.nebula.commerce.modules.product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.commerce.infrastructure.web.Result;
import com.nebula.commerce.modules.product.entity.Product;
import com.nebula.commerce.modules.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户前台 - 商品浏览接口
 * 权限要求: 无需登录或任意用户
 */
@RestController
@RequestMapping("/api/portal/product")
@RequiredArgsConstructor
public class ProductPortalController {

    private final ProductService productService;

    @GetMapping("/search")
    public Result<Page<Product>> search(@RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "10") Integer size,
                                        @RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) Long categoryId,
                                        @RequestParam(defaultValue = "new") String sort) {
        return Result.success(productService.searchProducts(page, size, keyword, categoryId, sort));
    }

    @GetMapping("/detail/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null || product.getStatus() != 1) {
            return Result.error("商品已下架或不存在");
        }
        return Result.success(product);
    }
}