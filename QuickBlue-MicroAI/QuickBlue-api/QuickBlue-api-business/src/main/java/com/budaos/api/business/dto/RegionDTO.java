package com.budaos.api.business.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 区域DTO
 *
 * @author budaos
 */
@Data
public class RegionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 区域ID
     */
    private Long regionId;

    /**
     * 区域名称
     */
    private String regionName;

    /**
     * 区域编码
     */
    private String regionCode;

    /**
     * 区域级别（1-国家 2-省份 3-城市 4-区县）
     */
    private Integer regionLevel;

    /**
     * 上级区域ID
     */
    private Long parentId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;

    /**
     * 状态（1-启用 2-禁用）
     */
    private Integer status;

    /**
     * 子区域列表
     */
    private List<RegionDTO> children;
}
