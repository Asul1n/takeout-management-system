package com.takeout.module.dish.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.takeout.common.constant.CommonConstant;
import com.takeout.common.exception.BusinessException;
import com.takeout.module.dish.dto.CategorySaveDTO;
import com.takeout.module.dish.entity.Category;
import com.takeout.module.dish.entity.Dish;
import com.takeout.module.dish.mapper.CategoryMapper;
import com.takeout.module.dish.mapper.DishMapper;
import com.takeout.module.dish.service.CategoryService;
import com.takeout.module.dish.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final DishMapper dishMapper;

    @Override
    public List<CategoryVO> listByMerchant(Long merchantId) {
        List<Category> list = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getMerchantId, merchantId)
                        .orderByAsc(Category::getSort));
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public CategoryVO add(Long merchantId, CategorySaveDTO dto) {
        Category category = new Category();
        category.setMerchantId(merchantId);
        BeanUtil.copyProperties(dto, category);
        categoryMapper.insert(category);
        return toVO(category);
    }

    @Override
    public CategoryVO update(Long merchantId, Long categoryId, CategorySaveDTO dto) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null || !category.getMerchantId().equals(merchantId)) {
            throw new BusinessException(CommonConstant.NOT_FOUND, "分类不存在");
        }
        BeanUtil.copyProperties(dto, category);
        categoryMapper.updateById(category);
        return toVO(category);
    }

    @Override
    public void delete(Long merchantId, Long categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null || !category.getMerchantId().equals(merchantId)) {
            throw new BusinessException(CommonConstant.NOT_FOUND, "分类不存在");
        }
        // 检查分类下是否有菜品
        Long count = dishMapper.selectCount(
                new LambdaQueryWrapper<Dish>().eq(Dish::getCategoryId, categoryId));
        if (count > 0) {
            throw new BusinessException("该分类下还有菜品，无法删除");
        }
        categoryMapper.deleteById(categoryId);
    }

    private CategoryVO toVO(Category c) {
        CategoryVO vo = new CategoryVO();
        BeanUtil.copyProperties(c, vo);
        return vo;
    }
}
