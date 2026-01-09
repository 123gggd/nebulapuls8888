package com.nebula.commerce.modules.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nebula.commerce.modules.product.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品分类 Mapper
 * [修复] 包路径修正为标准的 ...modules.product.mapper
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}