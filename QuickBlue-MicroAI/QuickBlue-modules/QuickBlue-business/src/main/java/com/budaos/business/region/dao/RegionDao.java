package com.budaos.business.region.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.business.region.domain.entity.RegionEntity;
import com.budaos.business.region.domain.form.RegionQueryForm;
import com.budaos.business.region.domain.vo.RegionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 区域表（树形结构） Dao
 *
 * @Author zhujw
 * @Date 2025-12-20 14:34:26
 * @Copyright v1.0
 */
@Mapper
public interface RegionDao extends BaseMapper<RegionEntity> {

    /**
     * 分页查询
     */
    List<RegionVO> queryPage(Page page, @Param("queryForm") RegionQueryForm queryForm);

    /**
     * 获取所有区域列表
     */
    List<RegionVO> listAll();

    /**
     * 根据父ID查询子区域
     */
    List<RegionEntity> selectByParentId(@Param("parentId") String parentId);

    /**
     * 统计子区域数量
     */
    int countChildren(@Param("parentId") String parentId);

    /**
     * 检查区域编码是否存在
     */
    boolean existsByRegionCode(@Param("regionCode") String regionCode);

    /**
     * 检查区域编码是否存在（排除自身）
     */
    boolean existsByRegionCodeAndIdNot(@Param("regionCode") String regionCode, @Param("id") String id);

    /**
     * 统计区域关联的影城数量
     */
    int countCinemaByRegionId(@Param("regionId") String regionId);

}
