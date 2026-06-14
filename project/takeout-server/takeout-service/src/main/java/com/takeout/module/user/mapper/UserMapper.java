package com.takeout.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.takeout.module.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("CALL sp_delete_user(#{userId})")
    void deleteUserByProcedure(@Param("userId") Long userId);

    /** View: 用户汇总（替代手动JOIN 4表） */
    @Select("SELECT * FROM v_user_summary WHERE 1=1 " +
            "AND (#{phone} IS NULL OR phone LIKE CONCAT('%',#{phone},'%')) " +
            "AND (#{role} IS NULL OR role=#{role}) " +
            "AND (#{status} IS NULL OR status=#{status}) " +
            "ORDER BY id DESC LIMIT #{offset}, #{size}")
    List<Map<String, Object>> selectUserSummaries(@Param("phone") String phone, @Param("role") String role,
                                                   @Param("status") String status, @Param("offset") long offset, @Param("size") long size);

    @Select("SELECT COUNT(*) FROM v_user_summary WHERE 1=1 " +
            "AND (#{phone} IS NULL OR phone LIKE CONCAT('%',#{phone},'%')) " +
            "AND (#{role} IS NULL OR role=#{role}) " +
            "AND (#{status} IS NULL OR status=#{status})")
    long countUserSummaries(@Param("phone") String phone, @Param("role") String role, @Param("status") String status);
}
