package com.takeout.module.merchant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MerchantApplyDTO {

    @NotBlank(message = "商家名称不能为空")
    private String name;

    @NotBlank(message = "联系电话不能为空")
    private String phone;

    @NotBlank(message = "省不能为空")
    private String province;

    @NotBlank(message = "市不能为空")
    private String city;

    @NotBlank(message = "区不能为空")
    private String district;

    @NotBlank(message = "详细地址不能为空")
    private String addressDetail;

    private String openTime = "09:00";
    private String closeTime = "21:00";
}
