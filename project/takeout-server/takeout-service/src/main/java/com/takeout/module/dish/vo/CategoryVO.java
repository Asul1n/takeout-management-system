package com.takeout.module.dish.vo;

import lombok.Data;

@Data
public class CategoryVO {

    private Long id;
    private Long merchantId;
    private String name;
    private Integer sort;
}
