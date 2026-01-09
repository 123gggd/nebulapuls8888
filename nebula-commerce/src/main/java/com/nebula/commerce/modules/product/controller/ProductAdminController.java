package com.nebula.commerce.modules.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.commerce.infrastructure.security.LoginUser;
import com.nebula.commerce.infrastructure.web.Result;
import com.nebula.commerce.modules.product.entity.Product;
import com.nebula.commerce.modules.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商家后台 - 商品管理接口
 * 优化：利用 DataScopeHandler 自动处理数据隔离，移除冗余代码
 *
 * @author Nebula Commerce Team
 */
@RestController
@RequestMapping("/api/admin/product")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MERCHANT')")
public class ProductAdminController {

    private final ProductService productService;

    @GetMapping("/list")
    public Result<Page<Product>> list(@RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "10") Integer size,
                                      @RequestParam(required = false) String name) {
        Page<Product> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(name)) {
            wrapper.like(Product::getName, name);
        }

        // [优化] 这里不需要手动判断 MERCHANT 角色并拼接 merchant_id
        // DataScopeHandler 会自动拦截 SQL 并追加 WHERE merchant_id = ?

        wrapper.orderByDesc(Product::getCreateTime);
        return Result.success(productService.page(pageParam, wrapper));
    }

    @PostMapping("/save")
    public Result<String> save(@RequestBody @Valid Product product) {
        Long merchantId = getMerchantId();

        // [安全] 强制绑定当前商户ID
        // 如果是 ADMIN (merchantId 为 null), 默认为自营 (0)
        product.setMerchantId(merchantId == null ? 0L : merchantId);

        // 如果是修改操作，需要校验归属权 (防止恶意修改他人商品ID)
        if (product.getId() != null) {
            Product exist = productService.getById(product.getId());
            if (exist == null) return Result.error("商品不存在");

            // 如果是商家，必须确保操作的是自己的商品
            if (merchantId != null && !merchantId.equals(exist.getMerchantId())) {
                return Result.error("无权修改非本店商品");
            }
        }

        productService.saveOrUpdateProduct(product);
        return Result.success("保存成功");
    }

    @GetMapping("/detail/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null) return Result.error("商品不存在");

        // [安全] 详情页也要校验归属权（防止直接通过ID遍历查询）
        Long merchantId = getMerchantId();
        if (merchantId != null && !merchantId.equals(product.getMerchantId())) {
            return Result.error("无权查看非本店商品");
        }

        return Result.success(product);
    }

    @PostMapping("/status/{status}")
    public Result<String> updateStatus(@RequestBody List<Long> ids, @PathVariable Integer status) {
        if (ids.isEmpty()) return Result.success("无操作");

        // Update 操作会被 DataScopeHandler 拦截，自动追加 AND merchant_id = ?
        // 所以这里直接调用 Service 即可，商家永远改不了别人的商品状态
        productService.updateStatus(ids, status);

        return Result.success("状态更新成功");
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        // Delete 操作同理，会被自动拦截
        boolean success = productService.removeById(id);
        if (!success) {
            return Result.error("删除失败或商品不存在");
        }
        return Result.success("删除成功");
    }

    /**
     * 获取当前登录用户的商家ID
     * @return 商家ID，如果是管理员则返回 null
     */
    private Long getMerchantId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return ((LoginUser) authentication.getPrincipal()).getMerchantId();
        }
        return null;
    }
}