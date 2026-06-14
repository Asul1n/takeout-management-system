package com.takeout.module.order.dto;

import lombok.Data;

@Data
public class OrderQueryDTO {

    private String status;
    private Long merchantId;
    private Integer page = 1;
    private Integer size = 10;
}
