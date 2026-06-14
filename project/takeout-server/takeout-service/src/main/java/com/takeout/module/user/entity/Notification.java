package com.takeout.module.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.takeout.framework.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知记录表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notification")
public class Notification extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收用户ID */
    private Long userId;

    /** 事件类型 */
    private String eventType;

    /** 通知标题 */
    private String title;

    /** 通知内容 */
    private String content;

    /** 关联对象ID */
    private String refId;

    /** 是否已读: 0未读/1已读 */
    private Integer isRead;
}
