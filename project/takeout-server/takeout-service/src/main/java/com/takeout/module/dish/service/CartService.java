package com.takeout.module.dish.service;

import com.takeout.module.dish.entity.CartItem;
import com.takeout.module.dish.vo.DishVO;

import java.util.List;

public interface CartService {

    List<CartItem> list(Long customerId);

    void add(Long customerId, Long dishId, Integer quantity);

    void updateQuantity(Long customerId, Long cartItemId, Integer quantity);

    void remove(Long customerId, Long cartItemId);

    void clear(Long customerId);
}
