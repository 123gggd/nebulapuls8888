package com.nebula.commerce.modules.product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nebula.commerce.modules.product.entity.Product;

import java.util.List;

/**
 * 商品服务接口
 */
public interface ProductService extends IService<Product> {

    /**
     * 保存或更新商品 (包含 SKU 处理)
     */
    boolean saveOrUpdateProduct(Product product);

    /**
     * 批量更新状态 (上架/下架)
     * [修复] 明确返回 boolean，与实现类保持一致
     */
    boolean updateStatus(List<Long> ids, Integer status);

    /**
     * 商品搜索
     */
    Page<Product> searchProducts(Integer page, Integer size, String keyword, Long categoryId, String sort);

    /**
     * 扣减库存
     */
    boolean decreaseStock(Long productId, Integer quantity);

    /**
     * 增加库存 (回滚)
     */
    boolean increaseStock(Long productId, Integer quantity);
}