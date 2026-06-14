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
        jdbcTemplate.update("CALL sp_accept_order(?,?)", orderNo, merchantId);
}

    @Override
    @Transactional
    public void prepareComplete(Long merchantId, String orderNo) {
        jdbcTemplate.update("CALL sp_prepare_complete(?,?)", orderNo, merchantId);
}

    @Override
    @Transactional
    public void cancelOrder(Long customerId, String orderNo) {
        jdbcTemplate.update("CALL sp_cancel_order(?,?)", orderNo, customerId);
}

    /** 顾客申请取消（商家已接单后） */
    public void requestCancel(Long customerId, String orderNo) {
        jdbcTemplate.update("CALL sp_request_cancel(?,?)", orderNo, customerId);
}

    /** 商家同意/拒绝取消申请 */
    @Transactional
    public void merchantCancel(Long merchantId, String orderNo, boolean approved) {
        jdbcTemplate.update("CALL sp_merchant_cancel_review(?,?,?)", orderNo, merchantId, approved ? 1 : 0);
}

    @Override
    @Transactional
    public void confirmReceipt(Long customerId, String orderNo) {
        jdbcTemplate.update("CALL sp_confirm_receipt(?,?)", orderNo, customerId);
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
