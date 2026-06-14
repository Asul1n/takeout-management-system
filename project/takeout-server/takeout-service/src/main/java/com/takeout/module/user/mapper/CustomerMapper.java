package com.takeout.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.takeout.module.user.entity.Customer;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {
}
