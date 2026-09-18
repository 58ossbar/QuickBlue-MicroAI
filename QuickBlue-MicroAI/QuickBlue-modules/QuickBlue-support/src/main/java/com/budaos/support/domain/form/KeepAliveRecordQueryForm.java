package com.budaos.support.domain.form;

import com.budaos.common.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 心跳记录查询表单
 *
 * @author budaos
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "心跳记录查询表单")
public class KeepAliveRecordQueryForm extends PageQuery {

    @Schema(description = "关键字")
    private String keywords;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;
}
