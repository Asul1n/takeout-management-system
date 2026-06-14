package com.takeout.module.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderSubmitDTO {

    @NotNull(message = "商家ID不能为空")
    private Long merchantId;

    @NotNull(message = "配送地址ID不能为空")
    private Long addressId;

    private String remark;

    @NotNull(message = "订单明细不能为空")
    private List<CartItemDTO> items;
}
