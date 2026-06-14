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
    private final SseService sseService;
    private final NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO submitOrder(Long customerId, OrderSubmitDTO dto) {
        // 1. 校验配送地址
        Address address = addressMapper.selectById(dto.getAddressId());
        if (address == null || !address.getCustomerId().equals(customerId)) {
            throw new BusinessException(CommonConstant.ADDRESS_INCOMPLETE, "配送地址不存在");
        }

        // 2. 校验商家
        Merchant merchant = merchantMapper.selectById(dto.getMerchantId());
        if (merchant == null || !"已通过".equals(merchant.getAuditStatus())) {
            throw new BusinessException(CommonConstant.NOT_FOUND, "商家不存在或未通过审核");
        }

        // 3. 查询购物车菜品，校验上架状态和库存
        List<CartItemDTO> items = dto.getItems();
        for (CartItemDTO item : items) {
            Dish dish = dishMapper.selectById(item.getDishId());
            if (dish == null || !"上架".equals(dish.getStatus())) {
                throw new BusinessException(CommonConstant.DISH_OFF_SHELF, "菜品【" + (dish != null ? dish.getName() : item.getDishId()) + "】已下架");
            }
            if (dish.getStock() < item.getQuantity()) {
                throw new BusinessException(CommonConstant.STOCK_INSUFFICIENT, "菜品【" + dish.getName() + "】库存不足");
            }
            if (!dish.getMerchantId().equals(dto.getMerchantId())) {
                throw new BusinessException("菜品【" + dish.getName() + "】不属于该商家");
            }
        }

        // 4. 计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItemDTO item : items) {
            Dish dish = dishMapper.selectById(item.getDishId());
            totalAmount = totalAmount.add(dish.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        // 5. 生成订单编号 (YYYYMMDD + 6位流水)
        String orderNo = generateOrderNo();

        // 6. 写入订单主表
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setCustomerId(customerId);
        order.setMerchantId(dto.getMerchantId());

        // 商家自动接单逻辑
        if (merchant.getAutoAccept() == 1) {
            order.setStatus("备餐中");
        } else {
            order.setStatus("已提交");
        }

        order.setTotalAmount(totalAmount);
        order.setRemark(dto.getRemark());
        order.setAddressId(dto.getAddressId());
        orderMapper.insert(order);

        // 7. 写入订单明细（价格快照）
        for (CartItemDTO item : items) {
            Dish dish = dishMapper.selectById(item.getDishId());
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderNo(orderNo);
            orderItem.setDishId(dish.getId());
            orderItem.setDishName(dish.getName());
            orderItem.setUnitPrice(dish.getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItemMapper.insert(orderItem);
        }

        // 8. 扣减库存（乐观锁）
        for (CartItemDTO item : items) {
            int affected = dishMapper.deductStock(item.getDishId(), item.getQuantity());
            if (affected == 0) {
                throw new BusinessException(CommonConstant.STOCK_INSUFFICIENT, "库存扣减失败，请重试");
            }
        }

        // 9. 清空对应的购物车项
        for (CartItemDTO item : items) {
            cartItemMapper.delete(new LambdaQueryWrapper<CartItem>()
                    .eq(CartItem::getCustomerId, customerId)
                    .eq(CartItem::getDishId, item.getDishId()));
        }

        log.info("订单提交成功: orderNo={}, customerId={}, amount={}", orderNo, customerId, totalAmount);

        // SSE推送 + 通知：告知商家有新订单
        Long merchantUserId = dto.getMerchantId();
        sseService.sendEvent(merchantUserId, "order:new",
                Map.of("orderNo", orderNo, "totalAmount", totalAmount, "status", order.getStatus()));
        notificationService.create(merchantUserId, "order_new",
                "新订单通知", "您有一条新订单（" + orderNo + "），金额 ¥" + totalAmount, orderNo);

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
