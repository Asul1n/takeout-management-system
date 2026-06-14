package com.takeout.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterDTO {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "用户身份不能为空")
    private String role;

    private String name;

    // ===== 商家专用字段 =====
    private String province;
    private String city;
    private String district;
    private String addressDetail;
    private String openTime;
    private String closeTime;
}
