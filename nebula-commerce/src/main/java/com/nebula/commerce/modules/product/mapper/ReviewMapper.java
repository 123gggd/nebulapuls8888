package com.nebula.commerce.modules.product.mapper; // [修改点] 去掉了 .service

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nebula.commerce.modules.product.entity.Review;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {
    // 继承 BaseMapper 后，自动拥有 CRUD 能力
}