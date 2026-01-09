package com.nebula.commerce.modules.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nebula.commerce.modules.product.entity.ProductSku;
import org.apache.ibatis.annotations.Mapper;

/**
 * SKU 库存单元 Mapper
 * 对应表: pms_sku
 * * [优化] 包路径修正为标准的 ...modules.product.mapper
 */
@Mapper
public interface ProductSkuMapper extends BaseMapper<ProductSku> {
}