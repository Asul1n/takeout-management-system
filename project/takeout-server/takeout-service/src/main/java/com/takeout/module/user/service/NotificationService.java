package com.takeout.module.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.takeout.module.user.entity.Notification;

public interface NotificationService {

    /** 获取通知列表 */
    Page<Notification> list(Long userId, Integer page, Integer size);

    /** 标记已读 */
    void markRead(Long userId, Long notificationId);

    /** 全部标记已读 */
    void markAllRead(Long userId);

    /** 未读数量 */
    Long unreadCount(Long userId);

    /** 创建通知 */
    void create(Long userId, String eventType, String title, String content, String refId);
}
