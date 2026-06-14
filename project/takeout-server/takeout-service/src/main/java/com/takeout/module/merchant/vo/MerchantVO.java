package com.takeout.module.merchant.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MerchantVO {

    private Long id;
    private String name;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String addressDetail;
    private String openTime;
    private String closeTime;
    private String notice;
    private String bizStatus;
    private String auditStatus;
    private Integer monthlySales;
    private BigDecimal rating;
    private Integer autoAccept;
}
