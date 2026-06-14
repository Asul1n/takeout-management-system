package com.takeout.module.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.takeout.module.order.dto.OrderQueryDTO;
import com.takeout.module.order.dto.OrderSubmitDTO;
import com.takeout.module.order.vo.OrderDetailVO;
import com.takeout.module.order.vo.OrderVO;

public interface OrderService {

    /** 顾客提交订单 */
    OrderVO submitOrder(Long customerId, OrderSubmitDTO dto);

    /** 订单详情（含数据级权限校验） */
    OrderDetailVO detail(Long userId, String role, String orderNo);

    /** 订单详情（内部调用，无权限校验） */
    OrderDetailVO detail(String orderNo);

    /** 顾客：我的订单 */
    Page<OrderVO> listByCustomer(Long customerId, OrderQueryDTO dto);

    /** 商家：本店订单 */
    Page<OrderVO> listByMerchant(Long merchantId, OrderQueryDTO dto);

    /** 管理员：全平台订单 */
    Page<OrderVO> listAll(OrderQueryDTO dto);

    /** 商家接单 */
    void acceptOrder(Long merchantId, String orderNo);

    /** 商家备餐完成 */
    void prepareComplete(Long merchantId, String orderNo);

    /** 顾客取消订单（接单前直接取消） */
    void cancelOrder(Long customerId, String orderNo);

    /** 顾客申请取消（接单后需商家同意） */
    void requestCancel(Long customerId, String orderNo);

    /** 商家处理取消申请 */
    void merchantCancel(Long merchantId, String orderNo, boolean approved);

    /** 顾客确认收货 */
    void confirmReceipt(Long customerId, String orderNo);
}
