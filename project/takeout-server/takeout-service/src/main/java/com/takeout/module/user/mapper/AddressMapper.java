package com.takeout.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.takeout.module.user.entity.Address;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AddressMapper extends BaseMapper<Address> {

    /** 取消该顾客的所有默认地址 */
    @Update("UPDATE address SET is_default = 0 WHERE customer_id = #{customerId}")
    int clearDefault(@Param("customerId") Long customerId);
}
