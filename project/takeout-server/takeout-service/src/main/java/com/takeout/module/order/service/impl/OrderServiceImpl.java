package com.takeout.module.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.takeout.common.constant.CommonConstant;
import com.takeout.common.constant.OrderStatusEnum;
import com.takeout.common.exception.BusinessException;
import com.takeout.module.delivery.entity.Delivery;
import com.takeout.module.delivery.entity.Rider;
import com.takeout.module.delivery.mapper.DeliveryMapper;
import com.takeout.module.delivery.mapper.RiderMapper;
import com.takeout.module.dish.entity.CartItem;
import com.takeout.module.dish.entity.Dish;
import com.takeout.module.dish.mapper.CartItemMapper;
import com.takeout.module.dish.mapper.DishMapper;
import com.takeout.module.merchant.entity.Merchant;
import com.takeout.module.merchant.mapper.MerchantMapper;
import com.takeout.module.order.dto.CartItemDTO;
import com.takeout.module.order.dto.OrderQueryDTO;
import com.takeout.module.order.dto.OrderSubmitDTO;
import com.takeout.module.order.entity.Order;
import com.takeout.module.order.entity.OrderItem;
import com.takeout.module.order.mapper.OrderItemMapper;
import com.takeout.module.order.mapper.OrderMapper;
import com.takeout.module.order.service.OrderService;
import com.takeout.module.order.vo.OrderDetailVO;
import com.takeout.module.order.vo.OrderItemVO;
import com.takeout.module.order.vo.OrderVO;
import com.takeout.module.user.entity.Address;
import com.takeout.module.user.entity.Customer;
import com.takeout.module.user.entity.User;
import com.takeout.module.user.mapper.UserMapper;
import com.takeout.module.user.mapper.AddressMapper;
import com.takeout.module.user.mapper.CustomerMapper;
import com.takeout.module.user.service.NotificationService;
import com.takeout.module.user.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final DishMapper dishMapper;
    private final CartItemMapper cartItemMapper;
    private final AddressMapper addressMapper;
    private final CustomerMapper customerMapper;
    private final MerchantMapper merchantMapper;
    private final DeliveryMapper deliveryMapper;
    private final RiderMapper riderMapper;
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JdbcTemplate jdbcTemplate;
    private final SseService sseService;
    private final NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO submitOrder(Long customerId, OrderSubmitDTO dto) {
        List<CartItemDTO> items = dto.getItems();
        if (items.isEmpty()) throw new BusinessException("订单明细不能为空");

        // 准备5个菜品参数（存储过程最多支持5个）
        Long d1=null,d2=null,d3=null,d4=null,d5=null;
        Integer q1=0,q2=0,q3=0,q4=0,q5=0;
        for (int i=0; i<items.size() && i<5; i++) {
            CartItemDTO item = items.get(i);
            switch (i) {
                case 0 -> { d1=item.getDishId(); q1=item.getQuantity(); }
                case 1 -> { d2=item.getDishId(); q2=item.getQuantity(); }
                case 2 -> { d3=item.getDishId(); q3=item.getQuantity(); }
                case 3 -> { d4=item.getDishId(); q4=item.getQuantity(); }
                case 4 -> { d5=item.getDishId(); q5=item.getQuantity(); }
            }
        }

        // 调用存储过程（原子操作：校验库存→写订单→扣库存→清购物车）
        jdbcTemplate.update("CALL sp_submit_order(?,?,?,?,?,?,?,?,?,?,?,?,?,?,@ono)",
                customerId, dto.getMerchantId(), dto.getAddressId(), dto.getRemark(),
                d1, q1, d2, q2, d3, q3, d4, q4, d5, q5);
        String orderNo = jdbcTemplate.queryForObject("SELECT @ono", String.class);

        log.info("订单提交成功(SQL): orderNo={}, customerId={}", orderNo, customerId);

        // SSE推送 + 通知
        Long merchantUserId = dto.getMerchantId();
        sseService.sendEvent(merchantUserId, "order:new",
                Map.of("orderNo", orderNo, "status", "已提交"));
        notificationService.create(merchantUserId, "order_new",
                "新订单通知", "您有一条新订单（" + orderNo + "）", orderNo);

        return buildOrderVO(orderNo);
    }

    @Override
    public OrderDetailVO detail(Long userId, String role, String orderNo) {
        Order order = orderMapper.selectById(orderNo);
        if (order == null) {
            throw new BusinessException(CommonConstant.NOT_FOUND, "订单不存在");
        }
        // 数据级权限：仅顾客本人、对应商家、管理员可查看
        if (!"ADMIN".equals(role)
                && !order.getCustomerId().equals(userId)
                && !order.getMerchantId().equals(userId)) {
            throw new BusinessException(CommonConstant.FORBIDDEN, "无权限查看该订单");
        }
        return detail(orderNo);
    }

    @Override
    public OrderDetailVO detail(String orderNo) {
        Order order = orderMapper.selectById(orderNo);
        if (order == null) {
            throw new BusinessException(CommonConstant.NOT_FOUND, "订单不存在");
        }
        OrderVO base = buildOrderVO(orderNo);
        OrderDetailVO detailVO = new OrderDetailVO();
        BeanUtil.copyProperties(base, detailVO);

        // 查询配送信息
        Delivery delivery = deliveryMapper.selectOne(
                new LambdaQueryWrapper<Delivery>().eq(Delivery::getOrderNo, orderNo));
        if (delivery != null) {
            detailVO.setDeliveryStatus(delivery.getStatus());
            if (delivery.getRiderId() != null) {
                Rider rider = riderMapper.selectById(delivery.getRiderId());
                if (rider != null) {
                    detailVO.setRiderName(rider.getName());
                    User riderUser = userMapper.selectById(rider.getId());
                    detailVO.setRiderPhone(riderUser != null ? riderUser.getPhone() : "");
                }
            }
        }
        return detailVO;
    }

    @Override
    public Page<OrderVO> listByCustomer(Long customerId, OrderQueryDTO dto) {
        LambdaQueryWrapper<Order> wrapper = baseQuery(dto);
        wrapper.eq(Order::getCustomerId, customerId);
        wrapper.orderByDesc(Order::getCreateTime);
        return queryPage(dto, wrapper);
    }

    @Override
    public Page<OrderVO> listByMerchant(Long merchantId, OrderQueryDTO dto) {
        LambdaQueryWrapper<Order> wrapper = baseQuery(dto);
        wrapper.eq(Order::getMerchantId, merchantId);
        wrapper.orderByDesc(Order::getCreateTime);
        return queryPage(dto, wrapper);
    }

    @Override
    public Page<OrderVO> listAll(OrderQueryDTO dto) {
        LambdaQueryWrapper<Order> wrapper = baseQuery(dto);
        if (dto.getMerchantId() != null) {
            wrapper.eq(Order::getMerchantId, dto.getMerchantId());
        }
        wrapper.orderByDesc(Order::getCreateTime);
        return queryPage(dto, wrapper);
    }

    @Override
    @Transactional
    public void acceptOrder(Long merchantId, String orderNo) {
        Order order = orderMapper.selectById(orderNo);
        if (order == null || !order.getMerchantId().equals(merchantId)) {
            throw new BusinessException(CommonConstant.FORBIDDEN, "无权操作该订单");
        }
        validateStatusTransition(order, "备餐中");
        order.setStatus("备餐中");
        orderMapper.updateById(order);

        // SSE推送 + 通知：告知顾客订单已接单
        sseService.sendEvent(order.getCustomerId(), "order:accepted",
                Map.of("orderNo", orderNo, "status", "备餐中"));
        notificationService.create(order.getCustomerId(), "order_accepted",
                "订单已接单", "您的订单（" + orderNo + "）已被商家接单，正在备餐中", orderNo);
    }

    @Override
    @Transactional
    public void prepareComplete(Long merchantId, String orderNo) {
        Order order = orderMapper.selectById(orderNo);
        if (order == null || !order.getMerchantId().equals(merchantId)) {
            throw new BusinessException(CommonConstant.FORBIDDEN, "无权操作该订单");
        }
        validateStatusTransition(order, "待配送");
        order.setStatus("待配送");
        orderMapper.updateById(order);

        // 生成配送任务
        Delivery delivery = new Delivery();
        delivery.setOrderNo(orderNo);
        delivery.setStatus("待取餐");
        deliveryMapper.insert(delivery);

        log.info("备餐完成，配送任务已生成: orderNo={}", orderNo);

        // SSE推送 + 通知：告知顾客备餐完成
        sseService.sendEvent(order.getCustomerId(), "order:ready",
                Map.of("orderNo", orderNo, "status", "待配送"));
        notificationService.create(order.getCustomerId(), "order_ready",
                "备餐完成", "您的订单（" + orderNo + "）备餐已完成，等待骑手取餐", orderNo);
    }

    @Override
    @Transactional
    public void cancelOrder(Long customerId, String orderNo) {
        Order order = orderMapper.selectById(orderNo);
        if (order == null || !order.getCustomerId().equals(customerId)) {
            throw new BusinessException(CommonConstant.FORBIDDEN, "无权操作该订单");
        }

        // F19: 备餐中及之后状态需商家同意才能取消
        if (!"已提交".equals(order.getStatus()) && !"待接单".equals(order.getStatus())) {
            throw new BusinessException("商家已接单，请先申请商家同意取消");
        }

        validateStatusTransition(order, "已取消");
        deliveryMapper.delete(new LambdaQueryWrapper<Delivery>().eq(Delivery::getOrderNo, orderNo));
        order.setStatus("已取消");
        orderMapper.updateById(order);
        log.info("订单已取消: orderNo={}", orderNo);
    }

    /** 顾客申请取消（商家已接单后） */
    public void requestCancel(Long customerId, String orderNo) {
        Order order = orderMapper.selectById(orderNo);
        if (order == null || !order.getCustomerId().equals(customerId)) {
            throw new BusinessException(CommonConstant.FORBIDDEN, "无权操作该订单");
        }
        // 通知商家有取消申请
        notificationService.create(order.getMerchantId(), "order_ready",
                "取消申请", "顾客申请取消订单（" + orderNo + "），请处理", orderNo);
        log.info("顾客申请取消订单: orderNo={}", orderNo);
    }

    /** 商家同意/拒绝取消申请 */
    @Transactional
    public void merchantCancel(Long merchantId, String orderNo, boolean approved) {
        Order order = orderMapper.selectById(orderNo);
        if (order == null || !order.getMerchantId().equals(merchantId)) {
            throw new BusinessException(CommonConstant.FORBIDDEN, "无权操作该订单");
        }
        if (approved) {
            deliveryMapper.delete(new LambdaQueryWrapper<Delivery>().eq(Delivery::getOrderNo, orderNo));
            order.setStatus("已取消");
            orderMapper.updateById(order);
            notificationService.create(order.getCustomerId(), "delivery_arrived",
                    "订单已取消", "商家已同意取消您的订单（" + orderNo + "）", orderNo);
            log.info("商家同意取消: orderNo={}", orderNo);
        } else {
            notificationService.create(order.getCustomerId(), "delivery_arrived",
                    "取消被拒绝", "商家拒绝了您的取消申请（" + orderNo + "），订单将继续处理", orderNo);
            log.info("商家拒绝取消: orderNo={}", orderNo);
        }
    }

    @Override
    @Transactional
    public void confirmReceipt(Long customerId, String orderNo) {
        Order order = orderMapper.selectById(orderNo);
        if (order == null || !order.getCustomerId().equals(customerId)) {
            throw new BusinessException(CommonConstant.FORBIDDEN, "无权操作该订单");
        }
        validateStatusTransition(order, "已完成");

        order.setStatus("已完成");
        orderMapper.updateById(order);

        // 更新配送状态
        Delivery delivery = deliveryMapper.selectOne(
                new LambdaQueryWrapper<Delivery>().eq(Delivery::getOrderNo, orderNo));
        if (delivery != null) {
            delivery.setStatus("已送达");
            delivery.setDeliverTime(LocalDateTime.now());
            deliveryMapper.updateById(delivery);

            // 骑手设为空闲并累加配送数
            if (delivery.getRiderId() != null) {
                Rider rider = riderMapper.selectById(delivery.getRiderId());
                if (rider != null) {
                    rider.setStatus("空闲");
                    rider.setTotalDeliveries(rider.getTotalDeliveries() + 1);
                    riderMapper.updateById(rider);

                    // 骑手完成一单配送
                }
            }
        }

        log.info("订单已完成: orderNo={}", orderNo);

        // SSE推送 + 通知：告知顾客订单已完成
        sseService.sendEvent(order.getCustomerId(), "delivery:arrived",
                Map.of("orderNo", orderNo, "status", "已完成"));
        notificationService.create(order.getCustomerId(), "delivery_arrived",
                "订单已完成", "您的订单（" + orderNo + "）已确认收货，感谢您的惠顾！", orderNo);
    }

    // ==================== 私有方法 ====================

    private String generateOrderNo() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String counterKey = CommonConstant.ORDER_NO_PREFIX + today;
        // 确保 Redis 计数器已初始化（以 DB 中当日最大编号为基准）
        Long seq = redisTemplate.opsForValue().increment(counterKey);
        for (int retry = 0; retry < 100; retry++) {
            String orderNo = today + String.format("%06d", seq % 1000000);
            if (orderMapper.selectById(orderNo) == null) {
                return orderNo;
            }
            seq = redisTemplate.opsForValue().increment(counterKey);
        }
        throw new BusinessException("订单编号生成失败，请稍后重试");
    }

    private void validateStatusTransition(Order order, String targetStatus) {
        if (!OrderStatusEnum.canTransition(order.getStatus(), targetStatus)) {
            throw new BusinessException(CommonConstant.ORDER_STATUS_ERROR,
                    String.format("订单状态【%s】不允许变更为【%s】", order.getStatus(), targetStatus));
        }
    }

    private LambdaQueryWrapper<Order> baseQuery(OrderQueryDTO dto) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (dto.getStatus() != null && !dto.getStatus().isEmpty()) {
            wrapper.eq(Order::getStatus, dto.getStatus());
        }
        return wrapper;
    }

    private Page<OrderVO> queryPage(OrderQueryDTO dto, LambdaQueryWrapper<Order> wrapper) {
        Page<Order> page = new Page<>(dto.getPage(), dto.getSize());
        Page<Order> result = orderMapper.selectPage(page, wrapper);
        Page<OrderVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(o -> buildOrderVO(o.getOrderNo()))
                .collect(Collectors.toList()));
        return voPage;
    }

    private OrderVO buildOrderVO(String orderNo) {
        Order order = orderMapper.selectById(orderNo);
        if (order == null) return null;

        OrderVO vo = new OrderVO();
        vo.setOrderNo(order.getOrderNo());
        vo.setCustomerId(order.getCustomerId());
        vo.setMerchantId(order.getMerchantId());
        vo.setAddressId(order.getAddressId());
        vo.setStatus(order.getStatus());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setRemark(order.getRemark());
        vo.setCreateTime(order.getCreateTime());

        // 顾客姓名
        Customer customer = customerMapper.selectById(order.getCustomerId());
        if (customer != null) {
            vo.setCustomerName(customer.getName());
        }

        // 商家名称
        Merchant merchant = merchantMapper.selectById(order.getMerchantId());
        if (merchant != null) {
            vo.setMerchantName(merchant.getName());
        }

        // 地址
        Address addr = addressMapper.selectById(order.getAddressId());
        if (addr != null) {
            vo.setAddress(addr.getProvince() + addr.getCity() + addr.getDistrict() + addr.getDetail());
        }

        // 订单明细
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderNo, orderNo));
        List<OrderItemVO> itemVOs = new ArrayList<>();
        for (OrderItem item : items) {
            OrderItemVO itemVO = new OrderItemVO();
            BeanUtil.copyProperties(item, itemVO);
            itemVO.setSubtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            itemVOs.add(itemVO);
        }
        vo.setItems(itemVOs);

        return vo;
    }
}
