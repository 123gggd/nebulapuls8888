package com.nebula.commerce.modules.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nebula.commerce.modules.product.entity.Product;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品主表 Mapper
 * 对应表: sys_product
 * * [优化] 包路径修正为标准的 ...modules.product.mapper
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}