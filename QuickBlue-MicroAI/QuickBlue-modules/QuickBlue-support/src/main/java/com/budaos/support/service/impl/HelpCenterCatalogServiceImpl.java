package com.budaos.support.service.impl;

import com.budaos.common.core.domain.ApiResult;
import com.budaos.support.dao.HelpCenterCatalogDao;
import com.budaos.support.dao.HelpCenterDao;
import com.budaos.support.domain.entity.HelpCenterCatalogEntity;
import com.budaos.support.domain.form.HelpCenterCatalogAddForm;
import com.budaos.support.domain.form.HelpCenterCatalogUpdateForm;
import com.budaos.support.domain.vo.HelpCenterCatalogVO;
import com.budaos.support.domain.vo.HelpCenterVO;
import com.budaos.support.service.HelpCenterCatalogService;
import com.budaos.common.core.util.BeanCopyUtil;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 帮助文档目录服务实现
 *
 * @author budaos
 */
@Service
public class HelpCenterCatalogServiceImpl implements HelpCenterCatalogService {

    @Resource
    private HelpCenterCatalogDao helpDocCatalogDao;

    @Resource
    private HelpCenterDao helpDocDao;

    /**
     * 查询全部目录
     */
    @Override
    public List<HelpCenterCatalogVO> getAll() {
        return BeanCopyUtil.copyList(helpDocCatalogDao.selectList(null), HelpCenterCatalogVO.class);
    }

    /**
     * 添加目录
     */
    @Override
    public synchronized ApiResult<String> add(HelpCenterCatalogAddForm helpDocCatalogAddForm) {
        List<HelpCenterCatalogVO> helpDocCatalogList = getAll();
        Optional<HelpCenterCatalogVO> exist = helpDocCatalogList.stream()
                .filter(e -> helpDocCatalogAddForm.getName().equals(e.getName()))
                .findFirst();
        if (exist.isPresent()) {
            return ApiResult.userErrorParam("存在相同名称的目录了");
        }

        helpDocCatalogDao.insert(BeanCopyUtil.copyProperties(helpDocCatalogAddForm, HelpCenterCatalogEntity.class));
        return ApiResult.ok();
    }

    /**
     * 更新目录
     */
    @Override
    public synchronized ApiResult<String> update(HelpCenterCatalogUpdateForm updateForm) {
        HelpCenterCatalogEntity helpDocCatalogEntity = helpDocCatalogDao.selectById(updateForm.getHelpDocCatalogId());
        if (helpDocCatalogEntity == null) {
            return ApiResult.userErrorParam("目录不存在");
        }

        List<HelpCenterCatalogVO> helpDocCatalogList = getAll();
        Optional<HelpCenterCatalogVO> exist = helpDocCatalogList.stream()
                .filter(e -> updateForm.getName().equals(e.getName()))
                .findFirst();
        if (exist.isPresent() && !exist.get().getHelpDocCatalogId().equals(updateForm.getHelpDocCatalogId())) {
            return ApiResult.userErrorParam("存在相同名称的目录了");
        }
        helpDocCatalogDao.updateById(BeanCopyUtil.copyProperties(updateForm, HelpCenterCatalogEntity.class));
        return ApiResult.ok();
    }

    /**
     * 删除目录（如果有子目录、或者有帮助文档，则不能删除）
     */
    @Override
    public synchronized ApiResult<String> delete(Long helpDocCatalogId) {
        if (helpDocCatalogId == null) {
            return ApiResult.ok();
        }

        HelpCenterCatalogEntity helpDocCatalogEntity = helpDocCatalogDao.selectById(helpDocCatalogId);
        if (helpDocCatalogEntity == null) {
            return ApiResult.userErrorParam("目录不存在");
        }

        // 如果有子目录，则不能删除
        Optional<HelpCenterCatalogVO> existOptional = getAll().stream()
                .filter(e -> helpDocCatalogId.equals(e.getParentId()))
                .findFirst();
        if (existOptional.isPresent()) {
            return ApiResult.userErrorParam("存在子目录：" + existOptional.get().getName());
        }

        // 查询是否有帮助文档
        List<HelpCenterVO> helpDocVOList = helpDocDao.queryHelpDocByCatalogId(helpDocCatalogId);
        if (CollectionUtils.isNotEmpty(helpDocVOList)) {
            return ApiResult.userErrorParam("目录下存在文档，不能删除");
        }
        helpDocCatalogDao.deleteById(helpDocCatalogId);
        return ApiResult.ok();
    }

}
