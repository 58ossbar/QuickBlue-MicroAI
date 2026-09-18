package com.budaos.business.region.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 区域树形VO
 *
 * @Author zhujw
 * @Date 2025-12-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RegionTreeVO extends RegionVO {

    @Schema(description = "子区域")
    private List<RegionTreeVO> children;

    private Boolean matched;
}
