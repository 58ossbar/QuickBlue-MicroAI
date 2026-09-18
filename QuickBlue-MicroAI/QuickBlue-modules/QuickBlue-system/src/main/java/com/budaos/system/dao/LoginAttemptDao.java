package com.budaos.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.system.domain.entity.LoginAttemptEntity;
import com.budaos.system.domain.form.LoginAttemptQueryForm;
import com.budaos.system.domain.vo.LoginAttemptVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 登录失败DAO
 *
 * @author budaos
 */
@Mapper
public interface LoginAttemptDao extends BaseMapper<LoginAttemptEntity> {

    /**
     * 根据用户ID和用户类型查询
     *
     * @param userId   用户ID
     * @param userType 用户类型
     * @return 登录失败实体
     */
    LoginAttemptEntity selectByUserIdAndUserType(@Param("userId") Long userId, @Param("userType") Integer userType);

    /**
     * 根据用户ID和用户类型删除
     *
     * @param userId   用户ID
     * @param userType 用户类型
     */
    void deleteByUserIdAndUserType(@Param("userId") Long userId, @Param("userType") Integer userType);

    /**
     * 分页查询
     *
     * @param page      分页对象
     * @param queryForm 查询表单
     * @return 登录失败VO列表
     */
    List<LoginAttemptVO> queryPage(Page page, @Param("queryForm") LoginAttemptQueryForm queryForm);
}
