package com.takeout.module.dish.service;

import com.takeout.module.dish.dto.CategorySaveDTO;
import com.takeout.module.dish.vo.CategoryVO;

import java.util.List;

public interface CategoryService {

    List<CategoryVO> listByMerchant(Long merchantId);

    CategoryVO add(Long merchantId, CategorySaveDTO dto);

    CategoryVO update(Long merchantId, Long categoryId, CategorySaveDTO dto);

    void delete(Long merchantId, Long categoryId);
}
