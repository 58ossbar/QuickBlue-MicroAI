package com.budaos.business.notice.service;

import cn.hutool.core.util.StrUtil;

import com.budaos.business.notice.dao.AnnouncementTypeDao;
import com.budaos.business.notice.domain.entity.AnnouncementTypeEntity;
import com.budaos.business.notice.domain.vo.AnnouncementTypeVO;
import com.budaos.common.core.domain.ApiResult;
import com.budaos.common.core.util.BeanCopyUtil;
import jakarta.annotation.Resource;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 通知。公告 类型
 *
 */
@Service
public class AnnouncementTypeService {

    @Resource
    private AnnouncementTypeDao noticeTypeDao;

    /**
     * 查询全部
     * @return
     */
    public List<AnnouncementTypeVO> getAll() {
        return BeanCopyUtil.copyList(noticeTypeDao.selectList(null), AnnouncementTypeVO.class);
    }

    public AnnouncementTypeVO getByNoticeTypeId(Long noticceTypeId) {
        return BeanCopyUtil.copy(noticeTypeDao.selectById(noticceTypeId), AnnouncementTypeVO.class);
    }

    public synchronized ApiResult<String> add(String name) {
        if (StrUtil.isBlank(name)) {
            return ApiResult.userErrorParam("类型名称不能为空");
        }

        List<AnnouncementTypeEntity> noticeTypeEntityList = noticeTypeDao.selectList(null);
        if (!CollectionUtils.isEmpty(noticeTypeEntityList)) {
            boolean exist = noticeTypeEntityList.stream().map(AnnouncementTypeEntity::getNoticeTypeName).collect(Collectors.toSet()).contains(name);
            if (exist) {
                return ApiResult.userErrorParam("类型名称已经存在");
            }
        }
        noticeTypeDao.insert(AnnouncementTypeEntity.builder().noticeTypeName(name).build());
        return ApiResult.ok();
    }

    public synchronized ApiResult<String> update(Long noticeTypeId, String name) {
        if (StrUtil.isBlank(name)) {
            return ApiResult.userErrorParam("类型名称不能为空");
        }

        AnnouncementTypeEntity noticeTypeEntity = noticeTypeDao.selectById(noticeTypeId);
        if (noticeTypeEntity == null) {
            return ApiResult.userErrorParam("类型名称不存在");
        }

        List<AnnouncementTypeEntity> noticeTypeEntityList = noticeTypeDao.selectList(null);
        if (!CollectionUtils.isEmpty(noticeTypeEntityList)) {
            Optional<AnnouncementTypeEntity> optionalNoticeTypeEntity = noticeTypeEntityList.stream().filter(e -> e.getNoticeTypeName().equals(name)).findFirst();
            if (optionalNoticeTypeEntity.isPresent() && !optionalNoticeTypeEntity.get().getNoticeTypeId().equals(noticeTypeId)) {
                return ApiResult.userErrorParam("类型名称已经存在");
            }
        }
        noticeTypeEntity.setNoticeTypeName(name);
        noticeTypeDao.updateById(noticeTypeEntity);
        return ApiResult.ok();
    }

    public synchronized ApiResult<String> delete(Long noticeTypeId) {
        noticeTypeDao.deleteById(noticeTypeId);
        return ApiResult.ok();
    }

}
