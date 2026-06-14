package com.takeout.module.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.takeout.framework.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户账号表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 手机号（登录账号） */
    private String phone;

    /** 密码（bcrypt 加密） */
    private String password;

    /** 用户身份: ADMIN/MERCHANT/CUSTOMER/RIDER */
    private String role;

    /** 账号状态: 正常/禁用 */
    private String status;
}
