package com.budaos.business.notice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.api.system.dto.StaffDTO;
import com.budaos.api.system.feign.OrganizationFeignClient;
import com.budaos.api.system.feign.StaffFeignClient;
import com.budaos.business.notice.constant.AnnouncementVisibleRangeDataTypeEnum;
import com.budaos.business.notice.dao.AnnouncementDao;
import com.budaos.business.notice.domain.form.AnnouncementStaffQueryForm;
import com.budaos.business.notice.domain.form.AnnouncementViewRecordQueryForm;
import com.budaos.business.notice.domain.vo.*;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.core.util.BeanCopyUtil;
import com.budaos.common.core.util.PageConvertUtil;
import com.google.common.collect.Lists;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * 员工查看 通知。公告
 *

 */
@Service
public class AnnouncementStaffService {

    @Resource
    private AnnouncementDao noticeDao;

    @Resource
    private AnnouncementService noticeService;

    @Resource
    private OrganizationFeignClient departmentFeignClient;

    @Resource
    private StaffFeignClient employeeFeignClient;

    /**
     * 查询我的 通知、公告清单
     */
    public ApiResult<PageResponse<AnnouncementStaffVO>> queryList(Long requestEmployeeId, AnnouncementStaffQueryForm noticeEmployeeQueryForm) {
        Page<?> page = PageConvertUtil.convert2PageQuery(noticeEmployeeQueryForm);

        List<Long> employeeDepartmentIdList = Lists.newArrayList();
        ApiResult<StaffDTO> employeeResponse = employeeFeignClient.getById(requestEmployeeId);
        StaffDTO employeeDTO = employeeResponse != null && employeeResponse.getOk() ? employeeResponse.getData() : null;

        if (employeeDTO != null) {
            // 如果不是管理员 则获取请求人的 部门及其子部门
            if (!employeeDTO.getAdministratorFlag() && employeeDTO.getDepartmentId() != null) {
                ApiResult<List<Long>> deptIdListResponse = departmentFeignClient.getSelfAndChildrenIdList(employeeDTO.getDepartmentId());
                if (deptIdListResponse != null && deptIdListResponse.getOk() && deptIdListResponse.getData() != null) {
                    employeeDepartmentIdList = deptIdListResponse.getData();
                }
            }
        }

        List<AnnouncementStaffVO> noticeList = null;
        boolean administratorFlag = employeeDTO != null && employeeDTO.getAdministratorFlag();
        //只查询未读的
        if (noticeEmployeeQueryForm.getNotViewFlag() != null && noticeEmployeeQueryForm.getNotViewFlag()) {
            noticeList = noticeDao.queryEmployeeNotViewNotice(page,
                    requestEmployeeId,
                    noticeEmployeeQueryForm,
                    employeeDepartmentIdList,
                    false,
                    administratorFlag,
                    AnnouncementVisibleRangeDataTypeEnum.DEPARTMENT.getValue(),
                    AnnouncementVisibleRangeDataTypeEnum.EMPLOYEE.getValue());
        } else {
            // 查询全部
            noticeList = noticeDao.queryEmployeeNotice(page,
                    requestEmployeeId,
                    noticeEmployeeQueryForm,
                    employeeDepartmentIdList,
                    false,
                    administratorFlag,
                    AnnouncementVisibleRangeDataTypeEnum.DEPARTMENT.getValue(),
                    AnnouncementVisibleRangeDataTypeEnum.EMPLOYEE.getValue());
        }
        // 设置发布日期
        noticeList.forEach(notice -> notice.setPublishDate(notice.getPublishTime().toLocalDate()));

        return ApiResult.ok(PageConvertUtil.convert2PageResult(page, noticeList));
    }


    /**
     * 查询我的 待查看的 通知、公告清单
     */
    public ApiResult<AnnouncementDetailVO> view(Long requestEmployeeId, Long noticeId, String ip, String userAgent) {
        AnnouncementUpdateFormVO updateFormVO = noticeService.getUpdateFormVO(noticeId);
        if (updateFormVO == null || Boolean.TRUE.equals(updateFormVO.getDeletedFlag())) {
            return ApiResult.userErrorParam("通知公告不存在");
        }

        ApiResult<StaffDTO> employeeResponse = employeeFeignClient.getById(requestEmployeeId);
        StaffDTO employeeDTO = employeeResponse != null && employeeResponse.getOk() ? employeeResponse.getData() : null;
        if (employeeDTO == null) {
            return ApiResult.userErrorParam("员工不存在");
        }

        if (!updateFormVO.getAllVisibleFlag() && !checkVisibleRange(updateFormVO.getVisibleRangeList(), requestEmployeeId, employeeDTO.getDepartmentId())) {
            return ApiResult.userErrorParam("对不起，您没有权限查看内容");
        }

        AnnouncementDetailVO noticeDetailVO = BeanCopyUtil.copy(updateFormVO, AnnouncementDetailVO.class);
        long viewCount = noticeDao.viewRecordCount(noticeId, requestEmployeeId);

        if (viewCount == 0) {
            noticeDao.insertViewRecord(noticeId, requestEmployeeId, ip, userAgent, 1);
            // 该员工对于这个通知是第一次查看 页面浏览量+1 用户浏览量+1
            noticeDao.updateViewCount(noticeId, 1, 1);
            noticeDetailVO.setPageViewCount(noticeDetailVO.getPageViewCount() + 1);
            noticeDetailVO.setUserViewCount(noticeDetailVO.getUserViewCount() + 1);
            // 标记为已查看
            noticeDetailVO.setViewFlag(true);
        } else {
            noticeDao.updateViewRecord(noticeId, requestEmployeeId, ip, userAgent);
            // 该员工对于这个通知不是第一次查看 页面浏览量+1 用户浏览量+0
            noticeDao.updateViewCount(noticeId, 1, 0);
            noticeDetailVO.setPageViewCount(noticeDetailVO.getPageViewCount() + 1);
            // 已存在查看记录，标记为已查看
            noticeDetailVO.setViewFlag(true);
        }

        return ApiResult.ok(noticeDetailVO);
    }

    /**
     * 校验是否有查看权限的范围
     *
     */
    public boolean checkVisibleRange(List<AnnouncementVisibleRangeVO> visibleRangeList, Long employeeId, Long departmentId) {
        // 员工范围
        boolean anyMatch = visibleRangeList.stream().anyMatch(e -> AnnouncementVisibleRangeDataTypeEnum.EMPLOYEE.equalsValue(e.getDataType()) && Objects.equals(e.getDataId(), employeeId));
        if (anyMatch) {
            return true;
        }

        //部门范围
        List<Long> visibleDepartmentIdList = visibleRangeList.stream().filter(e -> AnnouncementVisibleRangeDataTypeEnum.DEPARTMENT.equalsValue(e.getDataType()))
                .map(AnnouncementVisibleRangeVO::getDataId).collect(Collectors.toList());

        for (Long visibleDepartmentId : visibleDepartmentIdList) {
            ApiResult<List<Long>> deptIdListResponse = departmentFeignClient.getSelfAndChildrenIdList(visibleDepartmentId);
            List<Long> departmentIdList = (deptIdListResponse != null && deptIdListResponse.getOk()) ? deptIdListResponse.getData() : null;
            if (departmentIdList != null && departmentIdList.contains(departmentId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 分页查询  查看记录
     */
    public PageResponse<AnnouncementViewRecordVO> queryViewRecord(AnnouncementViewRecordQueryForm noticeViewRecordQueryForm) {
        Page<?> page = PageConvertUtil.convert2PageQuery(noticeViewRecordQueryForm);
        List<AnnouncementViewRecordVO> noticeViewRecordList = noticeDao.queryNoticeViewRecordList(page, noticeViewRecordQueryForm);

        // 通过Feign调用获取员工和部门信息
        if (noticeViewRecordList != null && !noticeViewRecordList.isEmpty()) {
            noticeViewRecordList.forEach(record -> {
                if (record.getEmployeeId() != null) {
                    try {
                        // 获取员工信息
                        ApiResult<StaffDTO> employeeResponse = employeeFeignClient.getById(record.getEmployeeId());
                        if (employeeResponse != null && employeeResponse.getOk() && employeeResponse.getData() != null) {
                            record.setEmployeeName(employeeResponse.getData().getActualName());

                            // 获取部门信息
                            if (employeeResponse.getData().getDepartmentId() != null) {
                                ApiResult<?> deptResponse = departmentFeignClient.getById(employeeResponse.getData().getDepartmentId());
                                if (deptResponse != null && deptResponse.getOk() && deptResponse.getData() != null) {
                                    record.setDepartmentName(((com.budaos.api.system.dto.OrganizationDTO) deptResponse.getData()).getDepartmentName());
                                }
                            }
                        }
                    } catch (Exception e) {
                        // Feign调用失败不影响主流程，只记录日志
                        System.err.println("获取员工或部门信息失败: " + e.getMessage());
                    }
                }
            });
        }

        return PageConvertUtil.convert2PageResult(page, noticeViewRecordList);
    }
}
