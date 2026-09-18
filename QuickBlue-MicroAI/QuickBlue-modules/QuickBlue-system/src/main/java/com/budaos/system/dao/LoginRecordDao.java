package com.budaos.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.system.domain.entity.LoginRecordEntity;
import com.budaos.system.domain.form.LoginRecordQueryForm;
import com.budaos.system.domain.vo.LoginRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 登录日志 DAO
 *
 * @author budaos
 */
@Mapper
public interface LoginRecordDao extends BaseMapper<LoginRecordEntity> {

    /**
     * 分页查询
     *
     * @param page      分页对象
     * @param queryForm 查询表单
     * @return 登录日志VO列表
     */
    Page<LoginRecordVO> queryByPage(Page page, @Param("query") LoginRecordQueryForm queryForm);

    /**
     * 查询上一个登录记录
     *
     * @param userId          用户ID
     * @param userType        用户类型
     * @param loginLogResult  登录结果
     * @return 登录日志VO
     */
    LoginRecordVO queryLastByUserId(@Param("userId") Long userId, @Param("userType") Integer userType, @Param("loginLogResult") Integer loginLogResult);
}
