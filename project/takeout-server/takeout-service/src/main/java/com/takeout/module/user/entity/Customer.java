package com.takeout.module.user.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.takeout.framework.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 顾客信息表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("customer")
public class Customer extends BaseEntity {

    /** 顾客ID = 用户ID */
    @TableId
    private Long id;

    /** 姓名 */
    private String name;

    /** 默认配送地址ID */
    private Long defaultAddressId;
}
