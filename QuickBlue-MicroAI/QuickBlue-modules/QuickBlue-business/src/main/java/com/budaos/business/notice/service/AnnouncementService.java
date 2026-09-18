package com.budaos.business.notice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.budaos.api.system.dto.OrganizationDTO;
import com.budaos.api.system.dto.StaffDTO;
import com.budaos.api.system.feign.OrganizationFeignClient;
import com.budaos.api.system.feign.StaffFeignClient;
import com.budaos.business.notice.constant.DataChangeTraceTypeEnum;
import com.budaos.business.notice.constant.AnnouncementVisibleRangeDataTypeEnum;
import com.budaos.business.notice.dao.AnnouncementDao;
import com.budaos.business.notice.domain.entity.AnnouncementEntity;
import com.budaos.business.notice.domain.form.AnnouncementAddForm;
import com.budaos.business.notice.domain.form.AnnouncementQueryForm;
import com.budaos.business.notice.domain.form.AnnouncementUpdateForm;
import com.budaos.business.notice.domain.form.AnnouncementVisibleRangeForm;
import com.budaos.business.notice.domain.vo.AnnouncementTypeVO;
import com.budaos.business.notice.domain.vo.AnnouncementUpdateFormVO;
import com.budaos.business.notice.domain.vo.AnnouncementVO;
import com.budaos.business.notice.domain.vo.AnnouncementVisibleRangeVO;
import com.budaos.business.notice.manager.AnnouncementManager;
import com.budaos.common.core.constant.StringConstants;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.core.util.BeanCopyUtil;
import com.budaos.common.core.util.PageConvertUtil;
import com.google.common.collect.Maps;
import jakarta.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 通知。公告 后台管理业务
 *
 */
@Service
public class AnnouncementService {

    @Resource
    private AnnouncementDao noticeDao;

    @Resource
    private AnnouncementManager noticeManager;

    @Resource
    private AnnouncementTypeService noticeTypeService;

    @Resource
    private DataChangeTraceService dataTracerService;

    @Resource
    private OrganizationFeignClient departmentFeignClient;

    @Resource
    private StaffFeignClient employeeFeignClient;

    /**
     * 查询 通知、公告（带数据权限）
     * 
     * 数据权限说明：
     * - ME（仅本人）：只显示自己创建的通知
     * - DEPARTMENT（本部门）：显示本部门员工创建的通知
     * - DEPARTMENT_AND_SUB（本部门及以下）：显示本部门及子部门员工创建的通知
     * - ALL（全部）：显示所有通知
     *
     */
    public PageResponse<AnnouncementVO> query(AnnouncementQueryForm queryForm) {
        Page<?> page = PageConvertUtil.convert2PageQuery(queryForm);
        // 使用带数据权限的查询方法
        List<AnnouncementVO> list = noticeDao.queryWithDataScope(page, queryForm);
        LocalDateTime now = LocalDateTime.now();
        list.forEach(e -> e.setPublishFlag(e.getPublishTime().isBefore(now)));

        // 通过Feign调用获取创建人姓名
        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(noticeVO -> {
                if (noticeVO.getCreateUserId() != null) {
                    try {
                        ApiResult<StaffDTO> response = employeeFeignClient.getById(noticeVO.getCreateUserId());
                        if (response != null && response.getOk() && response.getData() != null) {
                            noticeVO.setCreateUserName(response.getData().getActualName());
                        }
                    } catch (Exception e) {
                        // Feign调用失败不影响主流程，只记录日志
                        System.err.println("获取创建人姓名失败: " + e.getMessage());
                    }
                }
            });
        }

        return PageConvertUtil.convert2PageResult(page, list);
    }

    /**
     * 添加
     */
    public ApiResult<String> add(AnnouncementAddForm addForm) {
        // 校验并获取可见范围
        ApiResult<String> validate = this.checkAndBuildVisibleRange(addForm);
        if (!validate.getOk()) {
            return ApiResult.error(validate);
        }

        // build 资讯
        AnnouncementEntity noticeEntity = BeanCopyUtil.copy(addForm, AnnouncementEntity.class);
        // 发布时间：不是定时发布时 默认为 当前
        if (!addForm.getScheduledPublishFlag()) {
            noticeEntity.setPublishTime(LocalDateTime.now());
        }
        // 保存数据
        noticeManager.save(noticeEntity, addForm.getVisibleRangeList());
        return ApiResult.ok();
    }

    /**
     * 校验并返回可见范围
     *
     */
    private ApiResult<String> checkAndBuildVisibleRange(AnnouncementAddForm form) {
        // 校验资讯分类
        AnnouncementTypeVO noticeType = noticeTypeService.getByNoticeTypeId(form.getNoticeTypeId());
        if (noticeType == null) {
            return ApiResult.userErrorParam("分类不存在");
        }

        if (form.getAllVisibleFlag()) {
            return ApiResult.ok();
        }

        /*
         * 校验可见范围
         * 非全部可见时 校验选择的员工|部门
         */
        List<AnnouncementVisibleRangeForm> visibleRangeUpdateList = form.getVisibleRangeList();
        if (CollectionUtils.isEmpty(visibleRangeUpdateList)) {
            return ApiResult.userErrorParam("未设置可见范围");
        }

        // 校验可见范围-> 员工
        List<Long> employeeIdList = visibleRangeUpdateList.stream()
                .filter(e -> AnnouncementVisibleRangeDataTypeEnum.EMPLOYEE.equalsValue(e.getDataType()))
                .map(AnnouncementVisibleRangeForm::getDataId)
                .distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(employeeIdList)) {
            employeeIdList = employeeIdList.stream().distinct().collect(Collectors.toList());
            // 使用 Feign Client 批量验证员工
            for (Long employeeId : employeeIdList) {
                ApiResult<?> response = employeeFeignClient.getById(employeeId);
                if (response == null || !response.getOk() || response.getData() == null) {
                    return ApiResult.userErrorParam("员工id不存在：" + employeeId);
                }
            }
        }

        // 校验可见范围-> 部门
        List<Long> deptIdList = visibleRangeUpdateList.stream()
                .filter(e -> AnnouncementVisibleRangeDataTypeEnum.DEPARTMENT.equalsValue(e.getDataType()))
                .map(AnnouncementVisibleRangeForm::getDataId)
                .distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deptIdList)) {
            deptIdList = deptIdList.stream().distinct().collect(Collectors.toList());
            // 使用 Feign Client 批量验证部门
            for (Long deptId : deptIdList) {
                ApiResult<OrganizationDTO> response = departmentFeignClient.getById(deptId);
                if (response == null || !response.getOk() || response.getData() == null) {
                    return ApiResult.userErrorParam("部门id不存在：" + deptId);
                }
            }
        }
        return ApiResult.ok();
    }


    /**
     * 更新
     *
     */
    public ApiResult<String> update(AnnouncementUpdateForm updateForm) {

        AnnouncementEntity oldNoticeEntity = noticeDao.selectById(updateForm.getNoticeId());
        if (oldNoticeEntity == null) {
            return ApiResult.userErrorParam("通知不存在");
        }

        // 校验并获取可见范围
        ApiResult<String> res = this.checkAndBuildVisibleRange(updateForm);
        if (!res.getOk()) {
            return ApiResult.error(res);
        }

        // 更新
        AnnouncementEntity noticeEntity = BeanCopyUtil.copy(updateForm, AnnouncementEntity.class);
        noticeManager.update(oldNoticeEntity, noticeEntity, updateForm.getVisibleRangeList());
        return ApiResult.ok();
    }


    /**
     * 删除
     *
     */
    public ApiResult<String> delete(Long noticeId) {
        AnnouncementEntity noticeEntity = noticeDao.selectById(noticeId);
        if (null == noticeEntity || noticeEntity.getDeletedFlag()) {
            return ApiResult.userErrorParam("通知公告不存在");
        }
        // 更新删除状态
        noticeDao.updateDeletedFlag(noticeId);
        dataTracerService.delete(noticeId, DataChangeTraceTypeEnum.OA_NOTICE);
        return ApiResult.ok();
    }

    /**
     * 获取更新表单用的详情
     */
    public AnnouncementUpdateFormVO getUpdateFormVO(Long noticeId) {
        AnnouncementEntity noticeEntity = noticeDao.selectById(noticeId);
        if (null == noticeEntity) {
            return null;
        }

        AnnouncementUpdateFormVO updateFormVO = BeanCopyUtil.copy(noticeEntity, AnnouncementUpdateFormVO.class);
        AnnouncementTypeVO noticeType = noticeTypeService.getByNoticeTypeId(noticeEntity.getNoticeTypeId());
        updateFormVO.setNoticeTypeName(noticeType.getNoticeTypeName());
        updateFormVO.setPublishFlag(updateFormVO.getPublishTime() != null && updateFormVO.getPublishTime().isBefore(LocalDateTime.now()));

        if (!updateFormVO.getAllVisibleFlag()) {
            List<AnnouncementVisibleRangeVO> noticeVisibleRangeList = noticeDao.queryVisibleRange(noticeId);
            List<Long> employeeIdList = noticeVisibleRangeList.stream().filter(e -> AnnouncementVisibleRangeDataTypeEnum.EMPLOYEE.getValue().equals(e.getDataType()))
                    .map(AnnouncementVisibleRangeVO::getDataId)
                    .collect(Collectors.toList());

            Map<Long, String> employeeNameMap = null;
            if (CollectionUtils.isNotEmpty(employeeIdList)) {
                employeeNameMap = Maps.newHashMap();
                for (Long employeeId : employeeIdList) {
                    ApiResult<StaffDTO> response = employeeFeignClient.getById(employeeId);
                    if (response != null && response.getOk() && response.getData() != null) {
                        StaffDTO employeeDTO = response.getData();
                        employeeNameMap.put(employeeId, employeeDTO.getActualName());
                    }
                }
            } else {
                employeeNameMap = Maps.newHashMap();
            }
            for (AnnouncementVisibleRangeVO noticeVisibleRange : noticeVisibleRangeList) {
                if (noticeVisibleRange.getDataType().equals(AnnouncementVisibleRangeDataTypeEnum.EMPLOYEE.getValue())) {
                    noticeVisibleRange.setDataName(employeeNameMap.getOrDefault(noticeVisibleRange.getDataId(), StringConstants.EMPTY));
                } else {
                    ApiResult<OrganizationDTO> response = departmentFeignClient.getById(noticeVisibleRange.getDataId());
                    OrganizationDTO departmentDTO = response != null && response.getOk() ? response.getData() : null;
                    noticeVisibleRange.setDataName(departmentDTO == null ? StringConstants.EMPTY : departmentDTO.getDepartmentName());
                }
            }
            updateFormVO.setVisibleRangeList(noticeVisibleRangeList);
        }
        return updateFormVO;
    }
}
