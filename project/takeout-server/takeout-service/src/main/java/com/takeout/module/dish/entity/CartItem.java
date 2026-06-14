package com.takeout.module.dish.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.takeout.framework.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 购物车表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cart_item")
public class CartItem extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 顾客ID */
    private Long customerId;

    /** 菜品ID */
    private Long dishId;

    /** 数量 */
    private Integer quantity;
}
