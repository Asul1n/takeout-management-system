package com.takeout.module.dish.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.takeout.module.dish.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {
}
