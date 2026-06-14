package com.takeout.module.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.takeout.framework.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 操作日志表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("operation_log")
public class OperationLog extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作用户ID */
    private Long userId;

    /** 操作用户身份 */
    private String userRole;

    /** 操作类型 */
    private String operation;

    /** 操作对象类型 */
    private String targetType;

    /** 操作对象ID */
    private String targetId;

    /** 操作详情 */
    private String detail;

    /** 操作IP */
    private String ip;

    /** 用户代理 */
    private String userAgent;
}
