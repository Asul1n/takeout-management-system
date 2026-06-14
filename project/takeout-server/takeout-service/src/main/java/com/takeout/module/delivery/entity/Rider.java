package com.takeout.module.delivery.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.takeout.framework.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 骑手信息表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("rider")
public class Rider extends BaseEntity {

    /** 骑手ID = 用户ID */
    @TableId
    private Long id;

    /** 姓名 */
    private String name;

    /** 配送状态: 空闲/配送中/离线 */
    private String status;

    /** 累计配送单数 */
    private Integer totalDeliveries;
}
