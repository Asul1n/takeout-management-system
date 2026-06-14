package com.takeout.module.dish.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.takeout.framework.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 菜品信息表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dish")
public class Dish extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商家ID */
    private Long merchantId;

    /** 分类ID */
    private Long categoryId;

    /** 菜品名称 */
    private String name;

    /** 价格 */
    private BigDecimal price;

    /** 描述 */
    private String description;

    /** 图片URL */
    private String imageUrl;

    /** 库存量 */
    private Integer stock;

    /** 上架状态: 上架/下架 */
    private String status;
}
