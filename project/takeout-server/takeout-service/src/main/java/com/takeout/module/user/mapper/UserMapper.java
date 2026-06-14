package com.takeout.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.takeout.module.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /** 调用存储过程安全删除用户（保留订单数据） */
    @org.apache.ibatis.annotations.Select("CALL sp_delete_user(#{userId})")
    void deleteUserByProcedure(@Param("userId") Long userId);
}
