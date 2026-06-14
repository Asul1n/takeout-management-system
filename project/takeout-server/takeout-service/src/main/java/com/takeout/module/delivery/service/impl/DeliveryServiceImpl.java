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
import org.springframework.jdbc.core.JdbcTemplate;
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
    private final JdbcTemplate jdbcTemplate;
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
        jdbcTemplate.update("CALL sp_rider_accept_delivery(?,?)", deliveryId, riderId);
}

    @Override
    @Transactional
    public void pickup(Long riderId, Long deliveryId) {
        jdbcTemplate.update("CALL sp_rider_pickup(?,?)", deliveryId, riderId);
}

    @Override
    @Transactional
    public void deliver(Long riderId, Long deliveryId) {
        jdbcTemplate.update("CALL sp_rider_deliver(?,?)", deliveryId, riderId);
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
