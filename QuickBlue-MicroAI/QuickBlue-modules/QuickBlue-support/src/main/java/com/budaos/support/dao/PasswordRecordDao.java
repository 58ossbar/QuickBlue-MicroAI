package com.budaos.support.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.budaos.support.domain.entity.PasswordRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PasswordRecordDao extends BaseMapper<PasswordRecordEntity> {

    /**
     * 查询最后一次修改密码记录
     */
    PasswordRecordEntity selectLastByUserTypeAndUserId(@Param("userType") Integer userType, @Param("userId") Long userId);

    /**
     * 查询最近几次修改后的密码
     */
    List<String> selectOldPassword(@Param("userType") Integer userType, @Param("userId") Long userId, @Param("limit") int limit);

}
