package com.budaos.support.domain.form;

import com.budaos.common.core.domain.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据字典分页查询表单
 *
 * @author budaos
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DictionaryQueryForm extends PageQuery {

    private String keywords;

    private Boolean disabledFlag;

}
