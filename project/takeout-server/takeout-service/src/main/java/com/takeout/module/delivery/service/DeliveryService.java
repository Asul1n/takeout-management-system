package com.takeout.module.delivery.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.takeout.module.delivery.vo.DeliveryVO;

public interface DeliveryService {

    /** 骑手：待接配送任务 */
    Page<DeliveryVO> pendingTasks(Integer page, Integer size);

    /** 骑手：接单 */
    void acceptDelivery(Long riderId, Long deliveryId);

    /** 骑手：确认取餐 */
    void pickup(Long riderId, Long deliveryId);

    /** 骑手：确认送达 */
    void deliver(Long riderId, Long deliveryId);

    /** 骑手：配送记录 */
    Page<DeliveryVO> riderDeliveries(Long riderId, Integer page, Integer size);

    /** 管理员：全部配送记录 */
    Page<DeliveryVO> allDeliveries(Integer page, Integer size);
}
