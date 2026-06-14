package com.takeout.module.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.takeout.module.order.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}
