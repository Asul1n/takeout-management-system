package com.takeout.module.user.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 管理员信息表
 */
@Data
@TableName("admin")
public class Admin {

    /** 管理员ID = 用户ID */
    @TableId
    private Long id;

    /** 姓名 */
    private String name;
}
