package com.takeout.module.dish.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DishVO {

    private Long id;
    private Long merchantId;
    private Long categoryId;
    private String categoryName;
    private String name;
    private BigDecimal price;
    private String description;
    private String imageUrl;
    private Integer stock;
    private String status;
}
