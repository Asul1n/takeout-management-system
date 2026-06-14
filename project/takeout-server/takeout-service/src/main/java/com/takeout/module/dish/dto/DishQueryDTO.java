package com.takeout.module.dish.dto;

import lombok.Data;

@Data
public class DishQueryDTO {

    private Long merchantId;
    private Long categoryId;
    private String keyword;
    private Integer page = 1;
    private Integer size = 10;
}
