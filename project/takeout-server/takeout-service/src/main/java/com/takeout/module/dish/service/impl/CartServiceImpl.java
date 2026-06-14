package com.takeout.module.dish.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.takeout.common.constant.CommonConstant;
import com.takeout.common.exception.BusinessException;
import com.takeout.module.dish.entity.CartItem;
import com.takeout.module.dish.entity.Dish;
import com.takeout.module.dish.mapper.CartItemMapper;
import com.takeout.module.dish.mapper.DishMapper;
import com.takeout.module.dish.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemMapper cartItemMapper;
    private final DishMapper dishMapper;

    @Override
    public List<CartItem> list(Long customerId) {
        return cartItemMapper.selectList(
                new LambdaQueryWrapper<CartItem>().eq(CartItem::getCustomerId, customerId));
    }

    @Override
    public void add(Long customerId, Long dishId, Integer quantity) {
        // 校验菜品
        Dish dish = dishMapper.selectById(dishId);
        if (dish == null || !"上架".equals(dish.getStatus())) {
            throw new BusinessException(CommonConstant.DISH_OFF_SHELF, "菜品已下架");
        }
        if (dish.getStock() < quantity) {
            throw new BusinessException(CommonConstant.STOCK_INSUFFICIENT, "库存不足");
        }

        // 检查是否已存在（同一顾客+同一菜品），存在则合并数量
        CartItem existItem = cartItemMapper.selectOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getCustomerId, customerId)
                .eq(CartItem::getDishId, dishId));

        if (existItem != null) {
            existItem.setQuantity(existItem.getQuantity() + quantity);
            cartItemMapper.updateById(existItem);
        } else {
            CartItem item = new CartItem();
            item.setCustomerId(customerId);
            item.setDishId(dishId);
            item.setQuantity(quantity);
            cartItemMapper.insert(item);
        }
    }

    @Override
    public void updateQuantity(Long customerId, Long cartItemId, Integer quantity) {
        CartItem item = cartItemMapper.selectById(cartItemId);
        if (item == null || !item.getCustomerId().equals(customerId)) {
            throw new BusinessException(CommonConstant.NOT_FOUND, "购物车项不存在");
        }
        if (quantity <= 0) {
            cartItemMapper.deleteById(cartItemId);
        } else {
            item.setQuantity(quantity);
            cartItemMapper.updateById(item);
        }
    }

    @Override
    public void remove(Long customerId, Long cartItemId) {
        CartItem item = cartItemMapper.selectById(cartItemId);
        if (item == null || !item.getCustomerId().equals(customerId)) {
            throw new BusinessException(CommonConstant.NOT_FOUND, "购物车项不存在");
        }
        cartItemMapper.deleteById(cartItemId);
    }

    @Override
    public void clear(Long customerId) {
        cartItemMapper.delete(new LambdaQueryWrapper<CartItem>().eq(CartItem::getCustomerId, customerId));
    }
}
