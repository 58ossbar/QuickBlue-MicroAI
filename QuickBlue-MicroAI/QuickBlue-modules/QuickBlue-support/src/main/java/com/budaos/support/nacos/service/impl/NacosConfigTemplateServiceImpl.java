package com.budaos.support.nacos.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.common.core.domain.PageResponse;
import com.budaos.common.core.domain.CurrentUser;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.core.util.BeanCopyUtil;
import com.budaos.common.core.util.PageConvertUtil;
import com.budaos.common.core.util.RequestContextUtil;
import com.budaos.support.nacos.dao.NacosConfigTemplateDao;
import com.budaos.support.nacos.domain.entity.NacosConfigTemplateEntity;
import com.budaos.support.nacos.domain.form.NacosConfigTemplateAddForm;
import com.budaos.support.nacos.domain.form.NacosConfigTemplateQueryForm;
import com.budaos.support.nacos.domain.form.NacosConfigTemplateUpdateForm;
import com.budaos.support.nacos.domain.vo.NacosConfigTemplateVO;
import com.budaos.support.nacos.service.NacosConfigTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Nacos配置模板服务实现
 *
 * @author budaos
 * @since 2026-02-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NacosConfigTemplateServiceImpl implements NacosConfigTemplateService {

    private final NacosConfigTemplateDao templateDao;

    @Override
    public ApiResult<PageResponse<NacosConfigTemplateVO>> queryTemplatePage(NacosConfigTemplateQueryForm queryForm) {
        Page<NacosConfigTemplateEntity> page = PageConvertUtil.convert2PageQuery(queryForm);
        List<NacosConfigTemplateEntity> entityList = templateDao.queryByPage(page, queryForm);
        List<NacosConfigTemplateVO> voList = BeanCopyUtil.copyList(entityList, NacosConfigTemplateVO.class);
        
        PageResponse<NacosConfigTemplateVO> pageResult = new PageResponse<>();
        pageResult.setPageNum(page.getCurrent());
        pageResult.setPageSize(page.getSize());
        pageResult.setTotal(page.getTotal());
        pageResult.setList(voList);
        
        return ApiResult.ok(pageResult);
    }

    @Override
    public ApiResult<List<NacosConfigTemplateVO>> listAllTemplates() {
        List<NacosConfigTemplateEntity> entityList = templateDao.selectList(null);
        List<NacosConfigTemplateVO> voList = BeanCopyUtil.copyList(entityList, NacosConfigTemplateVO.class);
        return ApiResult.ok(voList);
    }

    @Override
    public ApiResult<NacosConfigTemplateVO> getTemplateById(Long templateId) {
        if (templateId == null) {
            return ApiResult.userErrorParam("模板ID不能为空");
        }
        
        NacosConfigTemplateEntity entity = templateDao.selectById(templateId);
        if (entity == null) {
            return ApiResult.userErrorParam("模板不存在");
        }
        
        NacosConfigTemplateVO vo = BeanCopyUtil.copyProperties(entity, NacosConfigTemplateVO.class);
        return ApiResult.ok(vo);
    }

    @Override
    public ApiResult<NacosConfigTemplateVO> getTemplateByCode(String templateCode) {
        if (!StringUtils.hasText(templateCode)) {
            return ApiResult.userErrorParam("模板编码不能为空");
        }
        
        NacosConfigTemplateEntity entity = templateDao.selectByTemplateCode(templateCode);
        if (entity == null) {
            return ApiResult.userErrorParam("模板不存在");
        }
        
        NacosConfigTemplateVO vo = BeanCopyUtil.copyProperties(entity, NacosConfigTemplateVO.class);
        return ApiResult.ok(vo);
    }

    @Override
    public ApiResult<String> addTemplate(NacosConfigTemplateAddForm addForm) {
        // 校验模板编码是否已存在
        NacosConfigTemplateEntity existEntity = templateDao.selectByTemplateCode(addForm.getTemplateCode());
        if (existEntity != null) {
            return ApiResult.userErrorParam("模板编码已存在");
        }
        
        // 构建实体
        NacosConfigTemplateEntity entity = BeanCopyUtil.copyProperties(addForm, NacosConfigTemplateEntity.class);
        
        // 设置默认类型
        if (!StringUtils.hasText(entity.getType())) {
            entity.setType("yaml");
        }
        
        // 设置创建人信息
        CurrentUser requestUser = RequestContextUtil.getRequestUser();
        if (requestUser != null) {
            entity.setCreateUserId(requestUser.getUserId());
            entity.setCreateUserName(requestUser.getUserName());
        }
        
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        
        // 保存
        templateDao.insert(entity);
        
        log.info("添加Nacos配置模板成功，模板ID: {}, 模板编码: {}", entity.getTemplateId(), entity.getTemplateCode());
        return ApiResult.ok();
    }

    @Override
    public ApiResult<String> updateTemplate(NacosConfigTemplateUpdateForm updateForm) {
        // 查询原模板
        NacosConfigTemplateEntity entity = templateDao.selectById(updateForm.getTemplateId());
        if (entity == null) {
            return ApiResult.userErrorParam("模板不存在");
        }
        
        // 校验模板编码是否被其他模板占用
        if (!Objects.equals(entity.getTemplateCode(), updateForm.getTemplateCode())) {
            NacosConfigTemplateEntity existEntity = templateDao.selectByTemplateCode(updateForm.getTemplateCode());
            if (existEntity != null) {
                return ApiResult.userErrorParam("模板编码已存在");
            }
        }
        
        // 更新属性
        entity.setTemplateName(updateForm.getTemplateName());
        entity.setTemplateCode(updateForm.getTemplateCode());
        entity.setDataId(updateForm.getDataId());
        entity.setGroupId(updateForm.getGroupId());
        entity.setContent(updateForm.getContent());
        entity.setType(StringUtils.hasText(updateForm.getType()) ? updateForm.getType() : "yaml");
        entity.setDescription(updateForm.getDescription());
        entity.setUpdateTime(LocalDateTime.now());
        
        // 保存
        templateDao.updateById(entity);
        
        log.info("更新Nacos配置模板成功，模板ID: {}", entity.getTemplateId());
        return ApiResult.ok();
    }

    @Override
    public ApiResult<String> deleteTemplate(Long templateId) {
        if (templateId == null) {
            return ApiResult.userErrorParam("模板ID不能为空");
        }
        
        NacosConfigTemplateEntity entity = templateDao.selectById(templateId);
        if (entity == null) {
            return ApiResult.userErrorParam("模板不存在");
        }
        
        templateDao.deleteById(templateId);
        
        log.info("删除Nacos配置模板成功，模板ID: {}", templateId);
        return ApiResult.ok();
    }

    @Override
    public ApiResult<List<NacosConfigTemplateVO>> listTemplatesByDatabaseType(String databaseType) {
        if (!StringUtils.hasText(databaseType)) {
            return ApiResult.userErrorParam("数据库类型不能为空");
        }
        
        List<NacosConfigTemplateEntity> entityList = templateDao.selectByDatabaseType(databaseType);
        List<NacosConfigTemplateVO> voList = BeanCopyUtil.copyList(entityList, NacosConfigTemplateVO.class);
        return ApiResult.ok(voList);
    }

    @Override
    public ApiResult<NacosConfigTemplateVO> getTemplateByDatabaseTypeAndCode(String databaseType, String templateCode) {
        if (!StringUtils.hasText(databaseType) || !StringUtils.hasText(templateCode)) {
            return ApiResult.userErrorParam("数据库类型和模板编码不能为空");
        }
        
        NacosConfigTemplateEntity entity = templateDao.selectByDatabaseTypeAndCode(databaseType, templateCode);
        if (entity == null) {
            return ApiResult.userErrorParam("模板不存在");
        }
        
        NacosConfigTemplateVO vo = BeanCopyUtil.copyProperties(entity, NacosConfigTemplateVO.class);
        return ApiResult.ok(vo);
    }
}
