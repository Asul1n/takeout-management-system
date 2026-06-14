package com.takeout.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressDTO {

    @NotBlank(message = "省不能为空")
    private String province;

    @NotBlank(message = "市不能为空")
    private String city;

    @NotBlank(message = "区不能为空")
    private String district;

    @NotBlank(message = "详细地址不能为空")
    private String detail;

    private Integer isDefault = 0;
}
