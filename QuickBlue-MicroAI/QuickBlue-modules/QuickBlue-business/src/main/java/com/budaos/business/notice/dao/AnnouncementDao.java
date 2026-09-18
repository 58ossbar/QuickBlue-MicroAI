package com.budaos.business.notice.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.business.notice.domain.entity.AnnouncementEntity;
import com.budaos.business.notice.domain.form.AnnouncementStaffQueryForm;
import com.budaos.business.notice.domain.form.AnnouncementQueryForm;
import com.budaos.business.notice.domain.form.AnnouncementViewRecordQueryForm;
import com.budaos.business.notice.domain.form.AnnouncementVisibleRangeForm;
import com.budaos.business.notice.domain.vo.AnnouncementStaffVO;
import com.budaos.business.notice.domain.vo.AnnouncementVO;
import com.budaos.business.notice.domain.vo.AnnouncementViewRecordVO;
import com.budaos.business.notice.domain.vo.AnnouncementVisibleRangeVO;
import com.budaos.common.core.annotation.DataPermission;
import com.budaos.common.core.enums.DataPermissionInTypeEnum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 公告、通知、新闻等等
 *

 */
@Mapper
public interface AnnouncementDao extends BaseMapper<AnnouncementEntity> {

    // ================================= 数据范围相关 【子表】 =================================

    /**
     * 保存可见范围
     *
     */
    void insertVisibleRange(@Param("noticeId") Long noticeId, @Param("visibleRangeFormList") List<AnnouncementVisibleRangeForm> visibleRangeFormList);

    /**
     * 删除可见范围
     *
     */
    void deleteVisibleRange(@Param("noticeId") Long noticeId);

    /**
     * 相关可见范围
     *
     */
    List<AnnouncementVisibleRangeVO> queryVisibleRange(@Param("noticeId") Long noticeId);

    // ================================= 通知公告【主表】 相关  =================================

    /**
     * 后管分页查询资讯
     *
     */
    List<AnnouncementVO> query(Page<?> page, @Param("query") AnnouncementQueryForm queryForm);

    /**
     * 后管分页查询资讯 - 应用数据权限
     * 使用 EMPLOYEE 类型的数据权限配置（基于创建人）
     * configCode 对应 t_data_scope_config 表的 config_code 字段
     * 
     * 数据权限说明：
     * - ME（仅本人）：只显示自己创建的通知
     * - DEPARTMENT（本部门）：显示本部门员工创建的通知
     * - DEPARTMENT_AND_SUB（本部门及以下）：显示本部门及子部门员工创建的通知
     * - ALL（全部）：显示所有通知
     *
     * @param page 分页参数
     * @param queryForm 查询条件
     * @return 通知公告列表
     */
    @DataPermission(
        configCode = "NOTICE",
        whereInType = DataPermissionInTypeEnum.EMPLOYEE,
        joinSql = "t_notice.create_user_id IN (#employeeIds)",
        whereIndex = 0,
        selfScopeColumn = "create_user_id"
    )
    List<AnnouncementVO> queryWithDataScope(Page<?> page, @Param("query") AnnouncementQueryForm queryForm);


    /**
     * 更新删除状态
     *
     */
    void updateDeletedFlag(@Param("noticeId") Long noticeId);

    // ================================= 通知公告【员工查看】 相关  =================================

    /**
     * 查询 员工 查看到的通知公告
     *
     */
    List<AnnouncementStaffVO> queryEmployeeNotice(Page<?> page,
                                               @Param("requestEmployeeId") Long requestEmployeeId,
                                               @Param("query") AnnouncementStaffQueryForm noticeEmployeeQueryForm,
                                               @Param("requestEmployeeDepartmentIdList") List<Long> requestEmployeeDepartmentIdList,
                                               @Param("deletedFlag") boolean deletedFlag,
                                               @Param("administratorFlag") boolean administratorFlag,
                                               @Param("departmentDataType") Integer departmentDataType,
                                               @Param("employeeDataType") Integer employeeDataType

    );

    /**
     * 查询 员工 未读的通知公告
     *
     */
    List<AnnouncementStaffVO> queryEmployeeNotViewNotice(Page<?> page,
                                               @Param("requestEmployeeId") Long requestEmployeeId,
                                               @Param("query") AnnouncementStaffQueryForm noticeEmployeeQueryForm,
                                               @Param("requestEmployeeDepartmentIdList") List<Long> requestEmployeeDepartmentIdList,
                                               @Param("deletedFlag") boolean deletedFlag,
                                               @Param("administratorFlag") boolean administratorFlag,
                                               @Param("departmentDataType") Integer departmentDataType,
                                               @Param("employeeDataType") Integer employeeDataType

    );

    long  viewRecordCount(@Param("noticeId")Long noticeId, @Param("employeeId")Long employeeId);

    /**
     * 查询通知、公告的 查看记录
     */
    List<AnnouncementViewRecordVO> queryNoticeViewRecordList(Page page, @Param("queryForm") AnnouncementViewRecordQueryForm noticeViewRecordQueryForm);

    /**
     * 保存查看记录
     */
    void insertViewRecord(@Param("noticeId") Long noticeId, @Param("employeeId") Long employeeId, @Param("ip") String ip, @Param("userAgent") String userAgent,@Param("pageViewCount") Integer pageViewCount);

    /**
     * 更新查看记录
     */
    void updateViewRecord(@Param("noticeId")Long noticeId, @Param("employeeId")Long requestEmployeeId,@Param("ip") String ip, @Param("userAgent")String userAgent);

    /**
     * 更新 浏览量
     *
     * @param noticeId 通知 id
     * @param pageViewCountIncrement 页面浏览量的增量
     * @param userViewCountIncrement 用户浏览量的增量
     */
    void updateViewCount(@Param("noticeId")Long noticeId,@Param("pageViewCountIncrement") Integer pageViewCountIncrement, @Param("userViewCountIncrement")Integer userViewCountIncrement);


}
