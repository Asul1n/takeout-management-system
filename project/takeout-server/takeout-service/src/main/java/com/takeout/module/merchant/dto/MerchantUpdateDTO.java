package com.takeout.module.merchant.dto;

import lombok.Data;

@Data
public class MerchantUpdateDTO {

    private String phone;
    private String province;
    private String city;
    private String district;
    private String addressDetail;
    private String openTime;
    private String closeTime;
    private String notice;
    private Integer autoAccept;
}
