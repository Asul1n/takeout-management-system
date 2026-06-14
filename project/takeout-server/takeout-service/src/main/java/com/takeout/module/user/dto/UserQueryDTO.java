package com.takeout.module.user.dto;

import lombok.Data;

@Data
public class UserQueryDTO {

    private String phone;
    private String role;
    private String status;
    private Integer page = 1;
    private Integer size = 10;
}
