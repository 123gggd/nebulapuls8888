package com.nebula.commerce.modules.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nebula.commerce.modules.product.entity.Product;
import com.nebula.commerce.modules.product.entity.ProductSku;
import com.nebula.commerce.modules.product.mapper.ProductMapper;
import com.nebula.commerce.modules.product.mapper.ProductSkuMapper;
import com.nebula.commerce.modules.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 商品服务实现类
 * 优化点：
 * 1. 使用 Java 21 var 语法
 * 2. 优化搜索条件构建逻辑
 * 3. 增强代码健壮性
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final ProductSkuMapper skuMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateProduct(Product product) {
        // 1. 保存/更新 SPU 主表
        if (product.getId() == null) {
            // 新增时默认上架
            if (product.getStatus() == null) product.setStatus(1);
            this.save(product);
        } else {
            this.updateById(product);
            // 更新时策略：简单起见，先删除旧 SKU 再插入新 SKU
            // 进阶优化：如果需要保留 SKU 的销售数据统计，这里应做 Diff (增删改) 处理
            skuMapper.delete(Wrappers.<ProductSku>lambdaQuery().eq(ProductSku::getProductId, product.getId()));
        }

        // 2. 保存 SKU 列表
        var skuList = product.getSkuList();
        if (skuList != null && !skuList.isEmpty()) {
            for (var sku : skuList) {
                sku.setProductId(product.getId());
                // 自动生成 SKU 编码
                if (!StringUtils.hasText(sku.getCode())) {
                    sku.setCode("SKU-" + System.currentTimeMillis() + "-" + Math.round(Math.random() * 1000));
                }
                skuMapper.insert(sku);
            }
        }
        return true;
    }

    @Override
    public boolean updateStatus(List<Long> ids, Integer status) {
        if (ids == null || ids.isEmpty()) return false;
        return this.lambdaUpdate()
                .set(Product::getStatus, status)
                .in(Product::getId, ids)
                .update();
    }

    @Override
    public Page<Product> searchProducts(Integer page, Integer size, String keyword, Long categoryId, String sort) {
        var pageParam = new Page<Product>(page, size);
        var wrapper = new LambdaQueryWrapper<Product>();

        // 1. 基础过滤：仅查上架商品
        wrapper.eq(Product::getStatus, 1);

        // 2. 关键词搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Product::getName, keyword)
                    .or()
                    .like(Product::getSubtitle, keyword));
        }

        // 3. 分类过滤
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }

        // 4. 排序逻辑 (Java 12+ switch expression 风格优化)
        if ("price_asc".equals(sort)) {
            wrapper.orderByAsc(Product::getPrice);
        } else if ("price_desc".equals(sort)) {
            wrapper.orderByDesc(Product::getPrice);
        } else {
            // 默认按创建时间倒序
            wrapper.orderByDesc(Product::getCreateTime);
        }

        return this.page(pageParam, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean decreaseStock(Long productId, Integer quantity) {
        if (quantity <= 0) return false;

        // 乐观锁扣减库存：利用数据库行锁保证原子性
        // update sys_product set stock = stock - ? where id = ? and stock >= ?
        return this.lambdaUpdate()
                .setSql("stock = stock - " + quantity)
                .eq(Product::getId, productId)
                .ge(Product::getStock, quantity) // 核心：防止超卖
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean increaseStock(Long productId, Integer quantity) {
        if (quantity <= 0) return false;

        return this.lambdaUpdate()
                .setSql("stock = stock + " + quantity)
                .eq(Product::getId, productId)
                .update();
    }
}