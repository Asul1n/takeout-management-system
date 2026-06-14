package com.takeout.module.merchant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.takeout.module.merchant.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MerchantMapper extends BaseMapper<Merchant> {
}
