package com.takeout.module.dish.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.takeout.common.constant.CommonConstant;
import com.takeout.common.exception.BusinessException;
import com.takeout.module.dish.dto.DishQueryDTO;
import com.takeout.module.dish.dto.DishSaveDTO;
import com.takeout.module.dish.entity.Category;
import com.takeout.module.dish.entity.Dish;
import com.takeout.module.dish.mapper.CategoryMapper;
import com.takeout.module.dish.mapper.DishMapper;
import com.takeout.module.dish.service.DishService;
import com.takeout.module.dish.vo.DishVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {

    private final DishMapper dishMapper;
    private final CategoryMapper categoryMapper;

    @Override
    public Page<DishVO> list(DishQueryDTO dto) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        if (dto.getMerchantId() != null) {
            wrapper.eq(Dish::getMerchantId, dto.getMerchantId());
        }
        if (dto.getCategoryId() != null) {
            wrapper.eq(Dish::getCategoryId, dto.getCategoryId());
        }
        if (dto.getKeyword() != null && !dto.getKeyword().isEmpty()) {
            wrapper.like(Dish::getName, dto.getKeyword());
        }
        wrapper.eq(Dish::getStatus, "上架");
        wrapper.orderByDesc(Dish::getCreateTime);

        Page<Dish> page = new Page<>(dto.getPage(), dto.getSize());
        Page<Dish> result = dishMapper.selectPage(page, wrapper);
        return (Page<DishVO>) result.convert(this::toVO);
    }

    @Override
    public DishVO detail(Long dishId) {
        Dish dish = dishMapper.selectById(dishId);
        if (dish == null) {
            throw new BusinessException(CommonConstant.NOT_FOUND, "菜品不存在");
        }
        return toVO(dish);
    }

    @Override
    public DishVO add(Long merchantId, DishSaveDTO dto) {
        // 校验同商家内菜名唯一
        Long count = dishMapper.selectCount(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getMerchantId, merchantId)
                .eq(Dish::getName, dto.getName()));
        if (count > 0) {
            throw new BusinessException("菜品名称已存在");
        }

        Dish dish = new Dish();
        dish.setMerchantId(merchantId);
        BeanUtil.copyProperties(dto, dish);
        dish.setStatus("上架");
        dishMapper.insert(dish);
        return toVO(dish);
    }

    @Override
    public DishVO update(Long merchantId, Long dishId, DishSaveDTO dto) {
        Dish dish = dishMapper.selectById(dishId);
        if (dish == null || !dish.getMerchantId().equals(merchantId)) {
            throw new BusinessException(CommonConstant.NOT_FOUND, "菜品不存在");
        }
        BeanUtil.copyProperties(dto, dish);
        dish.setId(dishId);
        dish.setMerchantId(merchantId);
        dishMapper.updateById(dish);
        return toVO(dish);
    }

    @Override
    public void toggleStatus(Long merchantId, Long dishId, String status) {
        Dish dish = dishMapper.selectById(dishId);
        if (dish == null || !dish.getMerchantId().equals(merchantId)) {
            throw new BusinessException(CommonConstant.NOT_FOUND, "菜品不存在");
        }
        dish.setStatus(status);
        dishMapper.updateById(dish);
    }

    @Override
    public void updateStock(Long merchantId, Long dishId, Integer stock) {
        Dish dish = dishMapper.selectById(dishId);
        if (dish == null || !dish.getMerchantId().equals(merchantId)) {
            throw new BusinessException(CommonConstant.NOT_FOUND, "菜品不存在");
        }
        dish.setStock(stock);
        if (stock == 0) {
            dish.setStatus("下架");
        }
        dishMapper.updateById(dish);
    }

    @Override
    public void updateImageUrl(Long dishId, String imageUrl) {
        Dish dish = dishMapper.selectById(dishId);
        if (dish != null) {
            dish.setImageUrl(imageUrl);
            dishMapper.updateById(dish);
        }
    }

    private DishVO toVO(Dish dish) {
        DishVO vo = new DishVO();
        BeanUtil.copyProperties(dish, vo);
        // 获取分类名称
        Category category = categoryMapper.selectById(dish.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }
        return vo;
    }
}
