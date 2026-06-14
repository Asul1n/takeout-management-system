package com.takeout.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.takeout.module.user.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
