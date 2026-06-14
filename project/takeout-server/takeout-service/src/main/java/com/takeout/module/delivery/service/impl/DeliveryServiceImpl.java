package com.takeout.module.delivery.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.takeout.common.constant.CommonConstant;
import com.takeout.common.exception.BusinessException;
import com.takeout.module.delivery.entity.Delivery;
import com.takeout.module.delivery.entity.Rider;
import com.takeout.module.delivery.mapper.DeliveryMapper;
import com.takeout.module.delivery.mapper.RiderMapper;
import com.takeout.module.delivery.service.DeliveryService;
import com.takeout.module.delivery.vo.DeliveryVO;
import com.takeout.module.merchant.entity.Merchant;
import com.takeout.module.merchant.mapper.MerchantMapper;
import com.takeout.module.order.entity.Order;
import com.takeout.module.order.entity.OrderItem;
import com.takeout.module.order.mapper.OrderItemMapper;
import com.takeout.module.order.mapper.OrderMapper;
import com.takeout.module.user.entity.Address;
import com.takeout.module.user.mapper.AddressMapper;
import com.takeout.module.user.service.NotificationService;
import com.takeout.module.user.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryMapper deliveryMapper;
    private final RiderMapper riderMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final AddressMapper addressMapper;
    private final MerchantMapper merchantMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SseService sseService;
    private final NotificationService notificationService;

    @Override
    public Page<DeliveryVO> pendingTasks(Integer page, Integer size) {
        LambdaQueryWrapper<Delivery> wrapper = new LambdaQueryWrapper<Delivery>()
                .isNull(Delivery::getRiderId)
                .eq(Delivery::getStatus, "待取餐")
                .orderByAsc(Delivery::getCreateTime);
        Page<Delivery> result = deliveryMapper.selectPage(new Page<>(page, size), wrapper);
        return (Page<DeliveryVO>) result.convert(this::toVO);
    }

    @Override
    @Transactional
    public void acceptDelivery(Long riderId, Long deliveryId) {
        // 骑手可同时接多单，不做互斥限制

        Delivery delivery = deliveryMapper.selectById(deliveryId);
        if (delivery == null || delivery.getRiderId() != null) {
            throw new BusinessException("该配送任务已被其他骑手接单");
        }

        // 更新配送记录
        delivery.setRiderId(riderId);
        delivery.setStatus("待取餐");
        deliveryMapper.updateById(delivery);

        // 更新骑手状态
        Rider rider = riderMapper.selectById(riderId);
        if (rider != null) {
            rider.setStatus("配送中");
            riderMapper.updateById(rider);
        }

        // 更新订单状态
        Order order = orderMapper.selectById(delivery.getOrderNo());
        if (order != null) {
            order.setStatus("配送中");
            orderMapper.updateById(order);
        }

        log.info("骑手接单: riderId={}, deliveryId={}, orderNo={}", riderId, deliveryId, delivery.getOrderNo());

        // SSE推送 + 通知：告知顾客骑手已接单
        if (order != null) {
            sseService.sendEvent(order.getCustomerId(), "delivery:accepted",
                    Map.of("orderNo", delivery.getOrderNo(), "riderName", rider.getName()));
            notificationService.create(order.getCustomerId(), "delivery_accepted",
                    "骑手已接单", "骑手" + rider.getName() + "已接单，正在前往取餐", delivery.getOrderNo());
        }
    }

    @Override
    @Transactional
    public void pickup(Long riderId, Long deliveryId) {
        Delivery delivery = getDeliveryById(deliveryId);
        if (!riderId.equals(delivery.getRiderId())) {
            throw new BusinessException(CommonConstant.FORBIDDEN, "无权操作该配送任务");
        }
        delivery.setStatus("配送中");
        delivery.setPickupTime(LocalDateTime.now());
        deliveryMapper.updateById(delivery);
    }

    @Override
    @Transactional
    public void deliver(Long riderId, Long deliveryId) {
        Delivery delivery = getDeliveryById(deliveryId);
        if (!riderId.equals(delivery.getRiderId())) {
            throw new BusinessException(CommonConstant.FORBIDDEN, "无权操作该配送任务");
        }
        delivery.setStatus("已送达");
        delivery.setDeliverTime(LocalDateTime.now());
        deliveryMapper.updateById(delivery);

        // 更新订单状态
        Order order = orderMapper.selectById(delivery.getOrderNo());
        if (order != null) {
            order.setStatus("已送达");
            orderMapper.updateById(order);
        }
    }

    @Override
    public Page<DeliveryVO> riderDeliveries(Long riderId, Integer page, Integer size) {
        LambdaQueryWrapper<Delivery> wrapper = new LambdaQueryWrapper<Delivery>()
                .eq(Delivery::getRiderId, riderId)
                .orderByDesc(Delivery::getCreateTime);
        Page<Delivery> result = deliveryMapper.selectPage(new Page<>(page, size), wrapper);
        return (Page<DeliveryVO>) result.convert(this::toVO);
    }

    @Override
    public Page<DeliveryVO> allDeliveries(Integer page, Integer size) {
        LambdaQueryWrapper<Delivery> wrapper = new LambdaQueryWrapper<Delivery>()
                .orderByDesc(Delivery::getCreateTime);
        Page<Delivery> result = deliveryMapper.selectPage(new Page<>(page, size), wrapper);
        return (Page<DeliveryVO>) result.convert(this::toVO);
    }

    private Delivery getDeliveryById(Long deliveryId) {
        Delivery delivery = deliveryMapper.selectById(deliveryId);
        if (delivery == null) {
            throw new BusinessException(CommonConstant.NOT_FOUND, "配送任务不存在");
        }
        return delivery;
    }

    private DeliveryVO toVO(Delivery d) {
        DeliveryVO vo = new DeliveryVO();
        vo.setId(d.getId());
        vo.setOrderNo(d.getOrderNo());
        vo.setRiderId(d.getRiderId());
        vo.setStatus(d.getStatus());
        vo.setPickupTime(d.getPickupTime());
        vo.setDeliverTime(d.getDeliverTime());
        vo.setCreateTime(d.getCreateTime());

        // 骑手姓名
        if (d.getRiderId() != null) {
            Rider rider = riderMapper.selectById(d.getRiderId());
            if (rider != null) vo.setRiderName(rider.getName());
        }

        // 商家地址
        Order order = orderMapper.selectById(d.getOrderNo());
        if (order != null) {
            Merchant merchant = merchantMapper.selectById(order.getMerchantId());
            if (merchant != null) {
                vo.setMerchantName(merchant.getName());
                vo.setMerchantAddress(merchant.getProvince() + merchant.getCity()
                        + merchant.getDistrict() + merchant.getAddressDetail());
            }
            Address addr = addressMapper.selectById(order.getAddressId());
            if (addr != null) {
                vo.setCustomerAddress(addr.getProvince() + addr.getCity()
                        + addr.getDistrict() + addr.getDetail());
            }
        }

        return vo;
    }
}
