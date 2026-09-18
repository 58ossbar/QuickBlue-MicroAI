package com.budaos.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.core.util.BeanCopyUtil;
import com.budaos.common.core.util.PageConvertUtil;
import com.budaos.system.dao.DataPermissionConfigDao;
import com.budaos.system.domain.entity.DataPermissionConfigEntity;
import com.budaos.system.domain.form.DataPermissionConfigAddForm;
import com.budaos.system.domain.form.DataPermissionConfigQueryForm;
import com.budaos.system.domain.form.DataPermissionConfigUpdateForm;
import com.budaos.system.domain.vo.DataPermissionConfigVO;
import com.budaos.system.service.DataPermissionConfigService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * 数据权限配置服务实现
 *
 * @author budaos
 */
@Slf4j
@Service
public class DataPermissionConfigServiceImpl implements DataPermissionConfigService {

    @Resource
    private DataPermissionConfigDao dataScopeConfigDao;

    @Override
    public ApiResult<List<DataPermissionConfigVO>> dataScopeConfigList() {
        try {
            List<DataPermissionConfigEntity> configList = dataScopeConfigDao.selectAllEnabled();

            List<DataPermissionConfigVO> voList = BeanCopyUtil.copyList(configList, DataPermissionConfigVO.class);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (int i = 0; i < voList.size(); i++) {
                DataPermissionConfigEntity entity = configList.get(i);
                DataPermissionConfigVO vo = voList.get(i);
                if (entity.getCreateTime() != null) {
                    vo.setCreateTime(entity.getCreateTime().format(formatter));
                }
            }

            return ApiResult.ok(voList);
        } catch (Exception e) {
            log.error("获取数据权限配置列表失败", e);
            return ApiResult.userErrorParam("获取数据权限配置列表失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResult<PageResponse<DataPermissionConfigVO>> queryPage(DataPermissionConfigQueryForm queryForm) {
        Page<DataPermissionConfigEntity> page = PageConvertUtil.convert2PageQuery(queryForm);

        LambdaQueryWrapper<DataPermissionConfigEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(queryForm.getConfigCode()), DataPermissionConfigEntity::getConfigCode, queryForm.getConfigCode())
                .like(StringUtils.isNotBlank(queryForm.getConfigName()), DataPermissionConfigEntity::getConfigName, queryForm.getConfigName())
                .like(StringUtils.isNotBlank(queryForm.getBusinessModule()), DataPermissionConfigEntity::getBusinessModule, queryForm.getBusinessModule())
                .eq(queryForm.getStatus() != null, DataPermissionConfigEntity::getStatus, queryForm.getStatus())
                .orderByAsc(DataPermissionConfigEntity::getSortOrder)
                .orderByDesc(DataPermissionConfigEntity::getCreateTime);

        Page<DataPermissionConfigEntity> entityPage = dataScopeConfigDao.selectPage(page, wrapper);

        List<DataPermissionConfigVO> voList = BeanCopyUtil.copyList(entityPage.getRecords(), DataPermissionConfigVO.class);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (int i = 0; i < voList.size(); i++) {
            DataPermissionConfigEntity entity = entityPage.getRecords().get(i);
            DataPermissionConfigVO vo = voList.get(i);
            if (entity.getCreateTime() != null) {
                vo.setCreateTime(entity.getCreateTime().format(formatter));
            }
        }

        PageResponse<DataPermissionConfigVO> pageResult = new PageResponse<>();
        pageResult.setPageNum(page.getCurrent());
        pageResult.setPageSize(page.getSize());
        pageResult.setTotal(page.getTotal());
        pageResult.setList(voList);
        return ApiResult.ok(pageResult);
    }

    @Override
    public ApiResult<String> add(DataPermissionConfigAddForm addForm) {
        // 校验编码是否重复
        DataPermissionConfigEntity existEntity = dataScopeConfigDao.selectByConfigCode(addForm.getConfigCode());
        if (existEntity != null) {
            return ApiResult.userErrorParam("配置编码已存在");
        }

        DataPermissionConfigEntity entity = BeanCopyUtil.copyProperties(addForm, DataPermissionConfigEntity.class);
        // 默认排序
        if (entity.getSortOrder() == null) {
            entity.setSortOrder(0);
        }
        // 默认启用
        if (entity.getStatus() == null) {
            entity.setStatus(true);
        }
        dataScopeConfigDao.insert(entity);
        return ApiResult.ok();
    }

    @Override
    public ApiResult<String> update(DataPermissionConfigUpdateForm updateForm) {
        DataPermissionConfigEntity entity = dataScopeConfigDao.selectById(updateForm.getConfigId());
        if (entity == null) {
            return ApiResult.userErrorParam("数据不存在");
        }

        // 校验编码是否重复
        DataPermissionConfigEntity existEntity = dataScopeConfigDao.selectByConfigCode(updateForm.getConfigCode());
        if (existEntity != null && !Objects.equals(existEntity.getConfigId(), updateForm.getConfigId())) {
            return ApiResult.userErrorParam("配置编码已存在");
        }

        entity = BeanCopyUtil.copyProperties(updateForm, DataPermissionConfigEntity.class);
        dataScopeConfigDao.updateById(entity);
        return ApiResult.ok();
    }

    @Override
    public ApiResult<String> delete(Long configId) {
        if (configId == null) {
            return ApiResult.ok();
        }
        dataScopeConfigDao.deleteById(configId);
        return ApiResult.ok();
    }

    @Override
    public ApiResult<String> batchDelete(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return ApiResult.ok();
        }
        dataScopeConfigDao.deleteBatchIds(idList);
        return ApiResult.ok();
    }

    @Override
    public ApiResult<String> updateStatus(Long configId) {
        DataPermissionConfigEntity entity = dataScopeConfigDao.selectById(configId);
        if (entity == null) {
            return ApiResult.userErrorParam("数据不存在");
        }
        entity.setStatus(!entity.getStatus());
        dataScopeConfigDao.updateById(entity);
        return ApiResult.ok();
    }
}
