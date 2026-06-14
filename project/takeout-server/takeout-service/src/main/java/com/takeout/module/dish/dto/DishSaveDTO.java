package com.takeout.module.dish.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DishSaveDTO {

    @NotBlank(message = "菜品名称不能为空")
    private String name;

    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    private String description;
    private String imageUrl;
    private Integer stock = 0;
}
