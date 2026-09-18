package com.budaos.support.domain.form;

import com.budaos.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据变动记录查询表单
 *
 * @author QuickBlue
 * @date 2026-02-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DataChangeTraceQueryForm extends PageQuery {

    /**
     * 业务类型
     */
    private Integer type;

    /**
     * 业务id
     */
    private Long dataId;

    /**
     * 关键字
     */
    private String keywords;
}
