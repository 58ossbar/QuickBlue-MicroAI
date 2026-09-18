package com.budaos.system.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.CurrentUser;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.core.util.BeanCopyUtil;
import com.budaos.common.excel.util.EasyExcelUtil;
import com.budaos.system.dao.OrganizationDao;
import com.budaos.system.dao.StaffDao;
import com.budaos.system.dao.JobPostDao;
import com.budaos.system.dao.AuthRoleStaffDao;
import com.budaos.system.domain.entity.OrganizationEntity;
import com.budaos.system.domain.entity.StaffEntity;
import com.budaos.system.domain.entity.JobPostEntity;
import com.budaos.system.domain.entity.AuthRoleStaffEntity;
import com.budaos.system.domain.excel.StaffExcel;
import com.budaos.system.domain.form.*;
import com.budaos.system.domain.vo.StaffVO;
import com.budaos.system.manager.StaffManager;
import com.budaos.system.service.StaffService;
import com.budaos.system.util.PasswordUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 员工服务实现
 *
 * @author budaos
 */
@Slf4j
@Service
public class StaffServiceImpl extends ServiceImpl<StaffDao, StaffEntity> implements StaffService {

    @Resource
    private StaffDao employeeDao;

    @Resource
    private OrganizationDao departmentDao;

    @Resource
    private JobPostDao positionDao;

    @Resource
    private AuthRoleStaffDao roleEmployeeDao;

    @Resource
    private StaffManager employeeManager;

    @Lazy
    @Resource
    private com.budaos.system.service.AuthService loginService;

    @Lazy
    @Resource
    private com.budaos.system.service.OrganizationService departmentService;

    @Override
    public StaffEntity getByLoginNameOrPhone(String loginNameOrPhone) {
        if (StrUtil.isBlank(loginNameOrPhone)) {
            return null;
        }
        LambdaQueryWrapper<StaffEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StaffEntity::getDeletedFlag, false)
                .and(w -> w.eq(StaffEntity::getLoginName, loginNameOrPhone)
                        .or().eq(StaffEntity::getPhone, loginNameOrPhone));
        return employeeDao.selectOne(wrapper);
    }

    @Override
    public StaffEntity getEmployeeDetail(Long employeeId) {
        if (employeeId == null) {
            return null;
        }
        return employeeDao.selectById(employeeId);
    }

    @Override
    public StaffVO getEmployeeDetailVO(Long employeeId) {
        if (employeeId == null) {
            return null;
        }
        StaffEntity employeeEntity = employeeDao.selectById(employeeId);
        if (employeeEntity == null) {
            return null;
        }
        List<StaffVO> voList = convertToEmployeeVOList(Collections.singletonList(employeeEntity));
        return voList.isEmpty() ? null : voList.get(0);
    }

    @Override
    public String generateSaltPassword(String password, String employeeUid) {
        if (StrUtil.isBlank(password) || StrUtil.isBlank(employeeUid)) {
            return password;
        }
        // 生成加盐密码：password_UID大写_UID小写
        return password + "_" + employeeUid.toUpperCase() + "_" + employeeUid.toLowerCase();
    }

    @Override
    public ApiResult<PageResponse<StaffVO>> queryEmployee(StaffQueryForm queryForm) {
        queryForm.setDeletedFlag(false);

        log.info("queryEmployee - departmentId: {}, includeSubDepartment: {}", 
            queryForm.getDepartmentId(), queryForm.getIncludeSubDepartment());

        Page<StaffEntity> pageParam = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());

        // 计算部门过滤条件（在SQL层面过滤，确保分页正确）
        List<Long> departmentIdList = null;
        if (queryForm.getDepartmentId() != null) {
            if (Boolean.TRUE.equals(queryForm.getIncludeSubDepartment())) {
                // 包含子部门：获取该部门及所有子部门ID
                departmentIdList = departmentService.selfAndChildrenIdList(queryForm.getDepartmentId());
                log.info("queryEmployee - 包含子部门, departmentIdList: {}", departmentIdList);
            } else {
                // 仅本级
                departmentIdList = Collections.singletonList(queryForm.getDepartmentId());
                log.info("queryEmployee - 仅本级, departmentIdList: {}", departmentIdList);
            }
        }

        // 使用带数据权限的查询方法，部门条件在SQL层面过滤
        IPage<StaffEntity> pageResult = employeeDao.queryByDataScopePage(pageParam, departmentIdList);
        
        log.info("queryEmployee - 查询结果 total: {}, records: {}", pageResult.getTotal(), pageResult.getRecords().size());

        // 手动过滤其他条件
        List<StaffEntity> records = pageResult.getRecords();
        if (StrUtil.isNotBlank(queryForm.getKeyword())) {
            String keyword = queryForm.getKeyword().toLowerCase();
            records = records.stream()
                    .filter(e -> (e.getLoginName() != null && e.getLoginName().toLowerCase().contains(keyword))
                            || (e.getActualName() != null && e.getActualName().toLowerCase().contains(keyword))
                            || (e.getPhone() != null && e.getPhone().toLowerCase().contains(keyword)))
                    .collect(Collectors.toList());
        }

        if (queryForm.getDisabledFlag() != null) {
            records = records.stream()
                    .filter(e -> queryForm.getDisabledFlag().equals(e.getDisabledFlag()))
                    .collect(Collectors.toList());
        }

        // 排序
        records.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));

        List<StaffVO> employeeVOList = convertToEmployeeVOList(records);

        PageResponse<StaffVO> result = new PageResponse<>();
        result.setPageNum(pageResult.getCurrent());
        result.setPageSize(pageResult.getSize());
        result.setTotal(pageResult.getTotal());
        result.setPageTotal(pageResult.getPages());
        result.setDataList(employeeVOList);

        return ApiResult.ok(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> addEmployee(StaffAddForm addForm) {
        // 校验登录名是否重复
        StaffEntity existEmployee = getByLoginName(addForm.getLoginName(), null);
        if (existEmployee != null) {
            return ApiResult.userErrorParam("登录名已存在");
        }

        // 校验手机号是否存在
        existEmployee = getByPhone(addForm.getPhone(), null);
        if (existEmployee != null) {
            return ApiResult.userErrorParam("手机号已存在");
        }

        // 校验邮箱是否存在
        if (StrUtil.isNotBlank(addForm.getEmail())) {
            existEmployee = getByEmail(addForm.getEmail(), null);
            if (existEmployee != null) {
                return ApiResult.userErrorParam("邮箱已存在");
            }
        }

        // 校验部门是否存在
        OrganizationEntity department = departmentDao.selectById(addForm.getDepartmentId());
        if (department == null) {
            return ApiResult.userErrorParam("部门不存在");
        }

        // 创建员工实体
        StaffEntity employee = BeanCopyUtil.copyProperties(addForm, StaffEntity.class);
        employee.setEmployeeUid(UUID.randomUUID().toString().replace("-", ""));
        employee.setDisabledFlag(addForm.getDisabledFlag());
        employee.setDeletedFlag(false);
        employee.setAdministratorFlag(false);

        // 生成随机密码
        String randomPassword = RandomUtil.randomNumbers(8);
        // 加盐：password_UID大写_UID小写
        String saltPassword = generateSaltPassword(randomPassword, employee.getEmployeeUid());
        // 使用 Argon2 加密
        String encryptedPassword = PasswordUtil.getEncryptPwd(saltPassword);
        employee.setLoginPwd(encryptedPassword);

        // 保存员工和角色关联
        employeeManager.saveEmployee(employee, addForm.getRoleIdList());

        log.info("新增员工成功，员工ID: {}, 登录名: {}", employee.getEmployeeId(), employee.getLoginName());

        return ApiResult.ok(randomPassword);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> updateEmployee(StaffUpdateForm updateForm) {
        StaffEntity employee = employeeDao.selectById(updateForm.getEmployeeId());
        if (employee == null) {
            return ApiResult.userErrorParam("员工不存在");
        }

        // 校验部门是否存在
        OrganizationEntity department = departmentDao.selectById(updateForm.getDepartmentId());
        if (department == null) {
            return ApiResult.userErrorParam("部门不存在");
        }

        // 检查唯一性
        ApiResult<String> checkResult = checkUniqueness(
                updateForm.getEmployeeId(),
                updateForm.getLoginName(),
                updateForm.getPhone(),
                updateForm.getEmail()
        );
        if (!checkResult.getOk()) {
            return checkResult;
        }

        // 更新员工信息
        StaffEntity employeeEntity = BeanCopyUtil.copyProperties(updateForm, StaffEntity.class);
        employeeEntity.setLoginPwd(null); // 不更新密码

        employeeManager.updateEmployee(employeeEntity, updateForm.getRoleIdList());

        // 清除员工缓存
        loginService.clearLoginEmployeeCache(employee.getEmployeeId());

        log.info("更新员工成功，员工ID: {}", employee.getEmployeeId());

        return ApiResult.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> updateCenter(StaffUpdateCenterForm updateCenterForm) {
        StaffEntity employee = employeeDao.selectById(updateCenterForm.getEmployeeId());
        if (employee == null) {
            return ApiResult.userErrorParam("员工不存在");
        }

        // 检查唯一性（不检查登录名）
        ApiResult<String> checkResult = checkUniqueness(
                updateCenterForm.getEmployeeId(),
                null,
                updateCenterForm.getPhone(),
                updateCenterForm.getEmail()
        );
        if (!checkResult.getOk()) {
            return checkResult;
        }

        // 更新员工信息
        StaffEntity employeeEntity = BeanCopyUtil.copyProperties(updateCenterForm, StaffEntity.class);
        employeeEntity.setLoginPwd(null);

        employeeDao.updateById(employeeEntity);

        // 清除员工缓存
        loginService.clearLoginEmployeeCache(employee.getEmployeeId());

        log.info("更新员工个人中心成功，员工ID: {}", employee.getEmployeeId());

        return ApiResult.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> updateAvatar(Long employeeId, String avatar) {
        StaffEntity employee = employeeDao.selectById(employeeId);
        if (employee == null) {
            return ApiResult.userErrorParam("员工不存在");
        }

        employee.setAvatar(avatar);
        employeeDao.updateById(employee);

        // 清除员工缓存
        loginService.clearLoginEmployeeCache(employeeId);

        return ApiResult.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> updateDisableFlag(Long employeeId) {
        StaffEntity employee = employeeDao.selectById(employeeId);
        if (employee == null) {
            return ApiResult.userErrorParam("员工不存在");
        }

        // 超级管理员不能禁用
        if (employee.getAdministratorFlag()) {
            return ApiResult.userErrorParam("超级管理员不能禁用");
        }

        employeeManager.updateDisabledFlag(employeeId, !employee.getDisabledFlag());

        // 清除员工缓存
        loginService.clearLoginEmployeeCache(employeeId);

        log.info("更新员工禁用状态成功，员工ID: {}, 禁用状态: {}",
                employeeId, !employee.getDisabledFlag());

        return ApiResult.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> batchDelete(List<Long> employeeIdList) {
        if (CollectionUtils.isEmpty(employeeIdList)) {
            return ApiResult.userErrorParam("请选择要删除的员工");
        }

        // 检查是否包含超级管理员
        for (Long employeeId : employeeIdList) {
            StaffEntity employee = employeeDao.selectById(employeeId);
            if (employee != null && employee.getAdministratorFlag()) {
                return ApiResult.userErrorParam("超级管理员不能删除");
            }
        }

        employeeManager.batchUpdateDeleteFlag(employeeIdList, true);

        // 清除所有员工的缓存
        for (Long employeeId : employeeIdList) {
            loginService.clearLoginEmployeeCache(employeeId);
        }

        log.info("批量删除员工成功，员工ID列表: {}", employeeIdList);

        return ApiResult.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> batchUpdateDepartment(List<Long> employeeIdList, Long departmentId) {
        if (CollectionUtils.isEmpty(employeeIdList)) {
            return ApiResult.userErrorParam("请选择要调整的员工");
        }

        // 校验部门是否存在
        OrganizationEntity department = departmentDao.selectById(departmentId);
        if (department == null) {
            return ApiResult.userErrorParam("部门不存在");
        }

        employeeManager.batchUpdateDepartment(employeeIdList, departmentId);

        // 清除所有员工的缓存
        for (Long employeeId : employeeIdList) {
            loginService.clearLoginEmployeeCache(employeeId);
        }

        log.info("批量调整员工部门成功，员工ID列表: {}, 新部门ID: {}", employeeIdList, departmentId);

        return ApiResult.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> updatePassword(CurrentUser requestUser, StaffUpdatePasswordForm updatePasswordForm) {
        if (!updatePasswordForm.getNewPassword().equals(updatePasswordForm.getConfirmPassword())) {
            return ApiResult.userErrorParam("两次密码输入不一致");
        }

        StaffEntity employee = employeeDao.selectById(requestUser.getUserId());
        if (employee == null) {
            return ApiResult.userErrorParam("员工不存在");
        }

        // 验证原密码
        String oldSaltPassword = generateSaltPassword(updatePasswordForm.getOldPassword(), employee.getEmployeeUid());
        if (!PasswordUtil.matches(oldSaltPassword, employee.getLoginPwd())) {
            return ApiResult.userErrorParam("原密码错误");
        }

        // 更新密码
        String newSaltPassword = generateSaltPassword(updatePasswordForm.getNewPassword(), employee.getEmployeeUid());
        String encryptedPassword = PasswordUtil.getEncryptPwd(newSaltPassword);
        employee.setLoginPwd(encryptedPassword);
        employeeDao.updateById(employee);

        // 清除员工缓存
        loginService.clearLoginEmployeeCache(employee.getEmployeeId());

        log.info("员工修改密码成功，员工ID: {}", employee.getEmployeeId());

        return ApiResult.ok();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> resetPassword(Long employeeId) {
        StaffEntity employee = employeeDao.selectById(employeeId);
        if (employee == null) {
            return ApiResult.userErrorParam("员工不存在");
        }

        // 超级管理员不能重置密码
        if (employee.getAdministratorFlag()) {
            return ApiResult.userErrorParam("超级管理员密码不能重置");
        }

        // 生成随机密码
        String randomPassword = RandomUtil.randomNumbers(8);
        // 加盐：password_UID大写_UID小写
        String saltPassword = generateSaltPassword(randomPassword, employee.getEmployeeUid());
        // 使用 Argon2 加密
        String encryptedPassword = PasswordUtil.getEncryptPwd(saltPassword);
        employee.setLoginPwd(encryptedPassword);
        employeeDao.updateById(employee);

        // 清除员工缓存
        loginService.clearLoginEmployeeCache(employeeId);

        log.info("重置员工密码成功，员工ID: {}, 登录名: {}", employeeId, employee.getLoginName());

        return ApiResult.ok(randomPassword);
    }

    @Override
    public ApiResult<List<StaffVO>> getAllEmployeeByDepartmentId(Long departmentId) {
        if (departmentId == null) {
            return ApiResult.ok(new ArrayList<>());
        }

        LambdaQueryWrapper<StaffEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StaffEntity::getDepartmentId, departmentId)
                .eq(StaffEntity::getDeletedFlag, false)
                .eq(StaffEntity::getDisabledFlag, false)
                .orderByAsc(StaffEntity::getActualName);

        List<StaffEntity> employeeList = employeeDao.selectList(wrapper);
        List<StaffVO> employeeVOList = convertToEmployeeVOList(employeeList);

        return ApiResult.ok(employeeVOList);
    }

    @Override
    public ApiResult<List<StaffVO>> queryAllEmployee(Boolean disabledFlag) {
        LambdaQueryWrapper<StaffEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StaffEntity::getDeletedFlag, false)
                .orderByAsc(StaffEntity::getActualName);

        if (disabledFlag != null) {
            wrapper.eq(StaffEntity::getDisabledFlag, disabledFlag);
        }

        List<StaffEntity> employeeList = employeeDao.selectList(wrapper);
        List<StaffVO> employeeVOList = convertToEmployeeVOList(employeeList);

        return ApiResult.ok(employeeVOList);
    }

    /**
     * 转换为EmployeeVO列表
     */
    private List<StaffVO> convertToEmployeeVOList(List<StaffEntity> employeeList) {
        if (CollectionUtils.isEmpty(employeeList)) {
            return new ArrayList<>();
        }

        List<StaffVO> employeeVOList = BeanCopyUtil.copyList(employeeList, StaffVO.class);

        // 查询部门信息
        List<Long> departmentIdList = employeeList.stream()
                .map(StaffEntity::getDepartmentId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, OrganizationEntity> departmentMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(departmentIdList)) {
            List<OrganizationEntity> departmentList = departmentDao.selectBatchIds(departmentIdList);
            departmentMap = departmentList.stream()
                    .collect(Collectors.toMap(OrganizationEntity::getDepartmentId, d -> d));
        }

        // 查询岗位信息
        List<Long> positionIdList = employeeList.stream()
                .map(StaffEntity::getPositionId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, JobPostEntity> positionMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(positionIdList)) {
            List<JobPostEntity> positionList = positionDao.selectBatchIds(positionIdList);
            positionMap = positionList.stream()
                    .collect(Collectors.toMap(JobPostEntity::getPositionId, p -> p));
        }

        // 查询员工角色
        List<Long> employeeIdList = employeeList.stream()
                .map(StaffEntity::getEmployeeId)
                .collect(Collectors.toList());
        List<AuthRoleStaffEntity> roleEmployeeList = roleEmployeeDao.selectList(
                new LambdaQueryWrapper<AuthRoleStaffEntity>()
                        .in(AuthRoleStaffEntity::getEmployeeId, employeeIdList)
        );

        // TODO: 查询角色名称（需要RoleDao）
        Map<Long, List<Long>> employeeRoleMap = roleEmployeeList.stream()
                .collect(Collectors.groupingBy(
                        AuthRoleStaffEntity::getEmployeeId,
                        Collectors.mapping(AuthRoleStaffEntity::getRoleId, Collectors.toList())
                ));

        // 填充VO
        for (StaffVO vo : employeeVOList) {
            // 部门名称
            OrganizationEntity department = departmentMap.get(vo.getDepartmentId());
            vo.setDepartmentName(department != null ? department.getDepartmentName() : null);

            // 岗位名称
            JobPostEntity position = positionMap.get(vo.getPositionId());
            vo.setPositionName(position != null ? position.getPositionName() : null);

            // 角色列表
            vo.setRoleIdList(employeeRoleMap.getOrDefault(vo.getEmployeeId(), new ArrayList<>()));
        }

        return employeeVOList;
    }

    /**
     * 根据登录名查询员工
     */
    private StaffEntity getByLoginName(String loginName, Long excludeEmployeeId) {
        LambdaQueryWrapper<StaffEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StaffEntity::getLoginName, loginName)
                .eq(StaffEntity::getDeletedFlag, false);
        if (excludeEmployeeId != null) {
            wrapper.ne(StaffEntity::getEmployeeId, excludeEmployeeId);
        }
        return employeeDao.selectOne(wrapper);
    }

    /**
     * 根据手机号查询员工
     */
    private StaffEntity getByPhone(String phone, Long excludeEmployeeId) {
        if (StrUtil.isBlank(phone)) {
            return null;
        }
        LambdaQueryWrapper<StaffEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StaffEntity::getPhone, phone)
                .eq(StaffEntity::getDeletedFlag, false);
        if (excludeEmployeeId != null) {
            wrapper.ne(StaffEntity::getEmployeeId, excludeEmployeeId);
        }
        return employeeDao.selectOne(wrapper);
    }

    /**
     * 根据邮箱查询员工
     */
    private StaffEntity getByEmail(String email, Long excludeEmployeeId) {
        if (StrUtil.isBlank(email)) {
            return null;
        }
        LambdaQueryWrapper<StaffEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StaffEntity::getEmail, email)
                .eq(StaffEntity::getDeletedFlag, false);
        if (excludeEmployeeId != null) {
            wrapper.ne(StaffEntity::getEmployeeId, excludeEmployeeId);
        }
        return employeeDao.selectOne(wrapper);
    }

    /**
     * 检查唯一性
     */
    private ApiResult<String> checkUniqueness(Long employeeId, String loginName, String phone, String email) {
        // 检查登录名
        if (StrUtil.isNotBlank(loginName)) {
            StaffEntity existEmployee = getByLoginName(loginName, employeeId);
            if (existEmployee != null) {
                return ApiResult.userErrorParam("登录名已存在");
            }
        }

        // 检查手机号
        if (StrUtil.isNotBlank(phone)) {
            StaffEntity existEmployee = getByPhone(phone, employeeId);
            if (existEmployee != null) {
                return ApiResult.userErrorParam("手机号已存在");
            }
        }

        // 检查邮箱
        if (StrUtil.isNotBlank(email)) {
            StaffEntity existEmployee = getByEmail(email, employeeId);
            if (existEmployee != null) {
                return ApiResult.userErrorParam("邮箱已存在");
            }
        }

        return ApiResult.ok();
    }

    @Override
    public void exportEmployee(HttpServletResponse response, Boolean disabledFlag) {
        // 查询员工数据
        ApiResult<List<StaffVO>> result = queryAllEmployee(disabledFlag);
        List<StaffVO> employeeList = result.getData();

        // 转换为Excel格式
        List<StaffExcel> employeeExcelList = new ArrayList<>();
        for (StaffVO employee : employeeList) {
            StaffExcel excel = new StaffExcel();
            excel.setLoginName(employee.getLoginName());
            excel.setActualName(employee.getActualName());
            excel.setGender(employee.getGender() != null ?
                    (employee.getGender() == 1 ? "男" : (employee.getGender() == 2 ? "女" : "")) : "");
            excel.setPhone(employee.getPhone());
            excel.setEmail(employee.getEmail());
            excel.setDepartmentName(employee.getDepartmentName());
            excel.setPositionName(employee.getPositionName());
            excel.setDisabledFlag(employee.getDisabledFlag() != null ?
                    (employee.getDisabledFlag() ? "是" : "否") : "否");
            excel.setRemark(employee.getRemark());
            employeeExcelList.add(excel);
        }

        // 生成文件名
        String fileName = "员工数据_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        // 导出Excel
        try {
            EasyExcelUtil.export(response, fileName, "员工数据", StaffExcel.class, employeeExcelList);
            log.info("导出员工数据成功，共{}条", employeeList.size());
        } catch (Exception e) {
            log.error("导出员工数据失败", e);
            throw new RuntimeException("导出员工数据失败: " + e.getMessage());
        }
    }

    @Override
    public void downloadImportTemplate(HttpServletResponse response) {
        // 创建模板示例数据
        List<StaffExcel> templateList = new ArrayList<>();
        StaffExcel template = new StaffExcel();
        template.setLoginName("zhangsan001");
        template.setActualName("张三");
        template.setGender("男");
        template.setPhone("13800138000");
        template.setEmail("zhangsan@example.com");
        template.setDepartmentName("票务分公司");
        template.setPositionName("核销员");
        template.setDisabledFlag("否");
        template.setRemark("示例员工，请参考此格式填写");
        templateList.add(template);

        // 导出Excel
        try {
            EasyExcelUtil.export(response, "员工导入模板", "员工数据", StaffExcel.class, templateList);
            log.info("下载员工导入模板成功");
        } catch (Exception e) {
            log.error("下载员工导入模板失败", e);
            throw new RuntimeException("下载员工导入模板失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> importEmployee(List<StaffExcel> employeeExcelList) {
        if (CollectionUtils.isEmpty(employeeExcelList)) {
            return ApiResult.userErrorParam("导入数据为空");
        }

        int successCount = 0;
        int failCount = 0;
        List<String> errorList = new ArrayList<>();

        for (int i = 0; i < employeeExcelList.size(); i++) {
            StaffExcel excel = employeeExcelList.get(i);
            int rowNum = i + 2; // Excel行号（从2开始，因为第1行是表头）
            List<String> rowErrors = new ArrayList<>();

            try {
                // 数据校验
                if (StrUtil.isBlank(excel.getLoginName())) {
                    rowErrors.add("登录账号不能为空");
                }
                if (StrUtil.isBlank(excel.getActualName())) {
                    rowErrors.add("真实姓名不能为空");
                }
                if (StrUtil.isBlank(excel.getPhone())) {
                    rowErrors.add("手机号码不能为空");
                } else if (!excel.getPhone().matches("^1[3-9]\\d{9}$")) {
                    rowErrors.add("手机号格式不正确");
                }

                if (StrUtil.isNotBlank(excel.getEmail()) && !excel.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                    rowErrors.add("邮箱格式不正确");
                }

                if (!rowErrors.isEmpty()) {
                    throw new IllegalArgumentException(String.join("、", rowErrors));
                }

                // 校验登录名是否已存在
                StaffEntity existEmployee = getByLoginName(excel.getLoginName(), null);
                if (existEmployee != null) {
                    throw new IllegalArgumentException("登录账号已存在");
                }

                // 校验手机号是否已存在
                existEmployee = getByPhone(excel.getPhone(), null);
                if (existEmployee != null) {
                    throw new IllegalArgumentException("手机号已存在");
                }

                // 校验邮箱是否已存在
                if (StrUtil.isNotBlank(excel.getEmail())) {
                    existEmployee = getByEmail(excel.getEmail(), null);
                    if (existEmployee != null) {
                        throw new IllegalArgumentException("邮箱已存在");
                    }
                }

                // 校验部门是否存在（必填）
                if (StrUtil.isBlank(excel.getDepartmentName())) {
                    throw new IllegalArgumentException("部门名称不能为空");
                }
                LambdaQueryWrapper<OrganizationEntity> deptWrapper = new LambdaQueryWrapper<>();
                deptWrapper.eq(OrganizationEntity::getDepartmentName, excel.getDepartmentName());
                OrganizationEntity department = departmentDao.selectOne(deptWrapper);
                if (department == null) {
                    throw new IllegalArgumentException("部门不存在，请填写正确的部门名称");
                }
                Long departmentId = department.getDepartmentId();

                // 校验岗位是否存在（选填）
                Long positionId = null;
                if (StrUtil.isNotBlank(excel.getPositionName())) {
                    LambdaQueryWrapper<JobPostEntity> positionWrapper = new LambdaQueryWrapper<>();
                    positionWrapper.eq(JobPostEntity::getPositionName, excel.getPositionName())
                            .eq(JobPostEntity::getDeletedFlag, false);
                    JobPostEntity position = positionDao.selectOne(positionWrapper);
                    if (position != null) {
                        positionId = position.getPositionId();
                    }
                }

                // 解析性别
                Integer gender = 1; // 默认男
                if (StrUtil.isNotBlank(excel.getGender())) {
                    if ("男".equals(excel.getGender()) || "1".equals(excel.getGender())) {
                        gender = 1;
                    } else if ("女".equals(excel.getGender()) || "2".equals(excel.getGender())) {
                        gender = 2;
                    } else {
                        throw new IllegalArgumentException("性别只能填写：男、女");
                    }
                }

                // 解析是否禁用
                Boolean disabledFlag = false;
                if (StrUtil.isNotBlank(excel.getDisabledFlag())) {
                    if ("是".equals(excel.getDisabledFlag()) || "1".equals(excel.getDisabledFlag())) {
                        disabledFlag = true;
                    } else if (!"否".equals(excel.getDisabledFlag()) && !"0".equals(excel.getDisabledFlag())) {
                        throw new IllegalArgumentException("是否禁用只能填写：是、否");
                    }
                }

                // 创建员工实体
                StaffEntity employee = new StaffEntity();
                employee.setEmployeeUid(UUID.randomUUID().toString().replace("-", ""));
                employee.setLoginName(excel.getLoginName());
                employee.setActualName(excel.getActualName());
                employee.setGender(gender);
                employee.setPhone(excel.getPhone());
                employee.setEmail(excel.getEmail());
                employee.setDepartmentId(departmentId);
                employee.setPositionId(positionId);
                employee.setDisabledFlag(disabledFlag);
                employee.setRemark(excel.getRemark());
                employee.setDeletedFlag(false);
                employee.setAdministratorFlag(false);

                // 生成随机密码
                String randomPassword = RandomUtil.randomNumbers(8);
                String saltPassword = generateSaltPassword(randomPassword, employee.getEmployeeUid());
                String encryptedPassword = PasswordUtil.getEncryptPwd(saltPassword);
                employee.setLoginPwd(encryptedPassword);

                // 保存员工
                employeeDao.insert(employee);

                successCount++;
                log.info("导入员工成功，第{}行，登录名: {}", rowNum, excel.getLoginName());

            } catch (Exception e) {
                failCount++;
                String errorMsg = String.format("第%d行 [%s]：%s", rowNum, excel.getActualName() != null ? excel.getActualName() : excel.getLoginName(), e.getMessage());
                errorList.add(errorMsg);
                log.error("导入员工数据失败: {}", errorMsg, e);
            }
        }

        // 构建结果消息
        StringBuilder message = new StringBuilder();
        message.append(String.format("导入完成！成功 %d 条，失败 %d 条", successCount, failCount));

        if (failCount > 0) {
            message.append("\n\n失败详情：\n");
            // 最多显示10条错误
            int showErrors = Math.min(errorList.size(), 10);
            for (int i = 0; i < showErrors; i++) {
                message.append(errorList.get(i)).append("\n");
            }
            if (errorList.size() > 10) {
                message.append(String.format("... 还有 %d 条错误未显示", errorList.size() - 10));
            }
        }

        log.info("员工导入完成，成功{}条，失败{}条", successCount, failCount);

        return ApiResult.ok(message.toString());
    }
}
