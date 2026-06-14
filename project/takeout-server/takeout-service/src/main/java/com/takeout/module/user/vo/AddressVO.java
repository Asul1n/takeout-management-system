package com.takeout.module.user.vo;

import lombok.Data;

@Data
public class AddressVO {

    private Long id;
    private Long customerId;
    private String province;
    private String city;
    private String district;
    private String detail;
    private Integer isDefault;
    private String fullAddress;
}
