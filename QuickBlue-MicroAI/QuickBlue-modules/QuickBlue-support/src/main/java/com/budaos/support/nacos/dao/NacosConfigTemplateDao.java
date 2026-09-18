package com.budaos.support.nacos.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.budaos.support.nacos.domain.entity.NacosConfigTemplateEntity;
import com.budaos.support.nacos.domain.form.NacosConfigTemplateQueryForm;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Nacos配置模板DAO
 *
 * @author budaos
 * @since 2026-02-24
 */
@Mapper
public interface NacosConfigTemplateDao extends BaseMapper<NacosConfigTemplateEntity> {

    /**
     * 分页查询配置模板
     *
     * @param page      分页参数
     * @param queryForm 查询条件
     * @return 模板列表
     */
    List<NacosConfigTemplateEntity> queryByPage(Page<NacosConfigTemplateEntity> page, @Param("query") NacosConfigTemplateQueryForm queryForm);

    /**
     * 根据模板编码查询
     *
     * @param templateCode 模板编码
     * @return 模板实体
     */
    NacosConfigTemplateEntity selectByTemplateCode(@Param("templateCode") String templateCode);

    /**
     * 根据数据库类型查询模板列表
     *
     * @param databaseType 数据库类型 (common/mysql/postgresql)
     * @return 模板列表
     */
    List<NacosConfigTemplateEntity> selectByDatabaseType(@Param("databaseType") String databaseType);

    /**
     * 根据数据库类型和模板编码查询
     *
     * @param databaseType 数据库类型
     * @param templateCode 模板编码
     * @return 模板实体
     */
    NacosConfigTemplateEntity selectByDatabaseTypeAndCode(@Param("databaseType") String databaseType,
                                                            @Param("templateCode") String templateCode);
}
