package com.takeout.module.dish.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.takeout.module.dish.dto.DishQueryDTO;
import com.takeout.module.dish.dto.DishSaveDTO;
import com.takeout.module.dish.vo.DishVO;

public interface DishService {

    Page<DishVO> list(DishQueryDTO dto);

    DishVO detail(Long dishId);

    DishVO add(Long merchantId, DishSaveDTO dto);

    DishVO update(Long merchantId, Long dishId, DishSaveDTO dto);

    void toggleStatus(Long merchantId, Long dishId, String status);

    void updateStock(Long merchantId, Long dishId, Integer stock);

    void updateImageUrl(Long dishId, String imageUrl);
}
