package com.budaos.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.CurrentUser;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.system.domain.entity.StaffEntity;
import com.budaos.system.domain.excel.StaffExcel;
import com.budaos.system.domain.form.*;
import com.budaos.system.domain.vo.StaffVO;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 员工服务接口
 *
 * @author budaos
 */
public interface StaffService extends IService<StaffEntity> {

    /**
     * 根据登录名或手机号查询员工
     *
     * @param loginNameOrPhone 登录名或手机号
     * @return 员工实体
     */
    StaffEntity getByLoginNameOrPhone(String loginNameOrPhone);

    /**
     * 根据员工ID查询员工详情
     *
     * @param employeeId 员工ID
     * @return 员工实体
     */
    StaffEntity getEmployeeDetail(Long employeeId);

    /**
     * 根据员工ID查询员工详情VO
     *
     * @param employeeId 员工ID
     * @return 员工VO
     */
    StaffVO getEmployeeDetailVO(Long employeeId);

    /**
     * 生成加盐密码
     *
     * @param password 密码
     * @param employeeUid 员工UID
     * @return 加盐后的密码
     */
    String generateSaltPassword(String password, String employeeUid);

    /**
     * 分页查询员工列表
     *
     * @param queryForm 查询表单
     * @return 员工列表
     */
    ApiResult<PageResponse<StaffVO>> queryEmployee(StaffQueryForm queryForm);

    /**
     * 添加员工
     *
     * @param addForm 添加表单
     * @return 初始密码
     */
    ApiResult<String> addEmployee(StaffAddForm addForm);

    /**
     * 更新员工
     *
     * @param updateForm 更新表单
     * @return 操作结果
     */
    ApiResult<String> updateEmployee(StaffUpdateForm updateForm);

    /**
     * 更新个人中心信息
     *
     * @param updateCenterForm 个人中心表单
     * @return 操作结果
     */
    ApiResult<String> updateCenter(StaffUpdateCenterForm updateCenterForm);

    /**
     * 更新头像
     *
     * @param employeeId 员工ID
     * @param avatar 头像URL
     * @return 操作结果
     */
    ApiResult<String> updateAvatar(Long employeeId, String avatar);

    /**
     * 更新禁用状态
     *
     * @param employeeId 员工ID
     * @return 操作结果
     */
    ApiResult<String> updateDisableFlag(Long employeeId);

    /**
     * 批量删除员工
     *
     * @param employeeIdList 员工ID列表
     * @return 操作结果
     */
    ApiResult<String> batchDelete(List<Long> employeeIdList);

    /**
     * 批量调整员工部门
     *
     * @param employeeIdList 员工ID列表
     * @param departmentId 部门ID
     * @return 操作结果
     */
    ApiResult<String> batchUpdateDepartment(List<Long> employeeIdList, Long departmentId);

    /**
     * 修改密码
     *
     * @param requestUser 当前用户
     * @param updatePasswordForm 密码表单
     * @return 操作结果
     */
    ApiResult<String> updatePassword(CurrentUser requestUser, StaffUpdatePasswordForm updatePasswordForm);

    /**
     * 重置员工密码
     *
     * @param employeeId 员工ID
     * @return 新密码
     */
    ApiResult<String> resetPassword(Long employeeId);

    /**
     * 根据部门ID查询员工列表
     *
     * @param departmentId 部门ID
     * @return 员工列表
     */
    ApiResult<List<StaffVO>> getAllEmployeeByDepartmentId(Long departmentId);

    /**
     * 查询所有员工
     *
     * @param disabledFlag 是否禁用
     * @return 员工列表
     */
    ApiResult<List<StaffVO>> queryAllEmployee(Boolean disabledFlag);

    /**
     * 导出员工数据到Excel
     *
     * @param response HTTP响应对象
     * @param disabledFlag 是否只导出禁用的员工
     */
    void exportEmployee(HttpServletResponse response, Boolean disabledFlag);

    /**
     * 下载员工导入模板
     *
     * @param response HTTP响应对象
     */
    void downloadImportTemplate(HttpServletResponse response);

    /**
     * 从Excel导入员工数据
     *
     * @param employeeExcelList 员工Excel数据列表
     * @return 导入结果
     */
    ApiResult<String> importEmployee(List<StaffExcel> employeeExcelList);
}

