package com.takeout.module.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemDTO {

    @NotNull(message = "菜品ID不能为空")
    private Long dishId;

    @NotNull(message = "数量不能为空")
    private Integer quantity;
}
