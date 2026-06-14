package com.takeout.module.dish.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.takeout.module.dish.entity.Dish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {

    /** 乐观锁扣减库存，返回影响行数 */
    @Update("UPDATE dish SET stock = stock - #{quantity} WHERE id = #{dishId} AND stock >= #{quantity}")
    int deductStock(@Param("dishId") Long dishId, @Param("quantity") int quantity);
}
