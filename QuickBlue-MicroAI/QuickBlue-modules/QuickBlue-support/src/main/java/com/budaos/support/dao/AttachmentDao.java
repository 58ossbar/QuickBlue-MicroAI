package com.budaos.support.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.support.domain.entity.AttachmentEntity;
import com.budaos.support.domain.form.AttachmentQueryForm;
import com.budaos.support.domain.vo.AttachmentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 文件数据访问层
 */
@Mapper
public interface AttachmentDao extends BaseMapper<AttachmentEntity> {

    /**
     * 文件key单个查询
     */
    AttachmentVO getByFileKey(@Param("fileKey") String fileKey);

    /**
     * 批量获取
     */
    List<AttachmentVO> selectByFileKeyList(@Param("fileKeyList") Collection<String> fileKeyList);

    /**
     * 分页查询
     */
    List<AttachmentVO> queryPage(Page page, @Param("queryForm") AttachmentQueryForm queryForm);

    /**
     * 根据文件key删除
     */
    int deleteByFileKey(@Param("fileKey") String fileKey);

    /**
     * 批量插入
     */
    int insertBatch(@Param("list") List<AttachmentEntity> fileEntities);

}
