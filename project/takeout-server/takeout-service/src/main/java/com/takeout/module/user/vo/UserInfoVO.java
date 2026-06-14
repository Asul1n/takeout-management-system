package com.takeout.module.user.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserInfoVO {

    private Long id;
    private String phone;
    private String role;
    private String status;
    private String name;
    private LocalDateTime createTime;
}
