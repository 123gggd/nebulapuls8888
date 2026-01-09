package com.nebula.commerce.modules.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nebula.commerce.modules.member.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
