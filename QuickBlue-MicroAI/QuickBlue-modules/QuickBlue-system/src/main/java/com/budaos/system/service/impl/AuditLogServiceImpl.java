package com.budaos.system.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.api.system.dto.AuditLogDTO;
import com.budaos.common.core.code.UserErrorCodes;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.core.util.BeanCopyUtil;
import com.budaos.common.core.util.PageConvertUtil;
import com.budaos.system.common.enums.AccountTypeEnum;
import com.budaos.system.dao.AuditLogDao;
import com.budaos.system.domain.entity.AuditLogEntity;
import com.budaos.system.domain.form.AuditLogQueryForm;
import com.budaos.system.domain.vo.AuditLogVO;
import com.budaos.system.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 操作日志服务实现
 *
 * @author budaos
 */
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogDao operateLogDao;

    @Override
    public ApiResult<PageResponse<AuditLogVO>> queryByPage(AuditLogQueryForm queryForm) {
        Page<AuditLogEntity> page = PageConvertUtil.convert2PageQuery(queryForm);
        List<AuditLogEntity> logEntityList = operateLogDao.queryByPage(page, queryForm);
        List<AuditLogVO> logVOList = BeanCopyUtil.copyList(logEntityList, AuditLogVO.class);
        PageResponse<AuditLogVO> pageResult = PageConvertUtil.convert2PageResult(page, logVOList);
        return ApiResult.ok(pageResult);
    }

    @Override
    public ApiResult<AuditLogVO> detail(Long operateLogId) {
        AuditLogEntity operateLogEntity = operateLogDao.selectById(operateLogId);
        if (operateLogEntity == null) {
            return ApiResult.error(UserErrorCodes.DATA_NOT_EXIST);
        }
        AuditLogVO operateLogVO = BeanCopyUtil.copyProperties(operateLogEntity, AuditLogVO.class);
        return ApiResult.ok(operateLogVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<String> batchDelete(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return ApiResult.error(UserErrorCodes.PARAM_ERROR, "日志ID列表不能为空");
        }
        operateLogDao.batchDelete(idList);
        return ApiResult.ok();
    }

    /**
     * 分页查询当前登录人信息
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    public ApiResult<PageResponse<AuditLogVO>> queryByPageLogin(AuditLogQueryForm queryForm) {
        Object user = StpUtil.getSession().get("requestUser");
        if (user instanceof com.budaos.system.domain.SessionEmployee) {
            com.budaos.system.domain.SessionEmployee requestEmployee = (com.budaos.system.domain.SessionEmployee) user;
            queryForm.setOperateUserId(requestEmployee.getUserId());
            queryForm.setOperateUserType(AccountTypeEnum.ADMIN_EMPLOYEE.getValue());
        }
        return queryByPage(queryForm);
    }

    @Override
    public void save(AuditLogEntity operateLogEntity) {
        operateLogDao.insert(operateLogEntity);
    }

    @Override
    public void save(AuditLogDTO operateLogDTO) {
        AuditLogEntity entity = new AuditLogEntity();
        BeanUtils.copyProperties(operateLogDTO, entity);
        // 如果操作人ID为空，设置默认值（表示系统/匿名用户）
        if (entity.getOperateUserId() == null) {
            entity.setOperateUserId(0L);
            entity.setOperateUserType(0);
            entity.setOperateUserName("系统");
        }
        operateLogDao.insert(entity);
    }
}
