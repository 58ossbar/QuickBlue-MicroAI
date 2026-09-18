package com.budaos.support.domain.form;

import com.budaos.common.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统更新日志查询表单
 *
 * @author budaos
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "系统更新日志查询表单")
public class ReleaseLogQueryForm extends PageQuery {

    @Schema(description = "版本")
    private String updateVersion;

    @Schema(description = "更新类型:[1:特大版本功能更新;2:功能更新;3:bug修复]")
    private Integer type;

    @Schema(description = "发布人")
    private String publishAuthor;
}
