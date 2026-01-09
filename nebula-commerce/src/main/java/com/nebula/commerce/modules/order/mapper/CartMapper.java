package com.nebula.commerce.modules.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nebula.commerce.modules.order.entity.Cart;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartMapper extends BaseMapper<Cart> {
}