package com.budaos.system.domain.form;

import com.budaos.common.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 操作日志查询表单
 *
 * @author budaos
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "操作日志查询表单")
public class AuditLogQueryForm extends PageQuery {

    @Schema(description = "用户ID")
    private Long operateUserId;

    @Schema(description = "用户类型")
    private Integer operateUserType;

    @Schema(description = "关键字：模块、操作内容")
    private String keywords;

    @Schema(description = "请求关键字：请求地址、请求方法、请求参数")
    private String requestKeywords;

    @Schema(description = "开始日期")
    private String startDate;

    @Schema(description = "结束日期")
    private String endDate;

    @Schema(description = "用户名称")
    private String userName;

    @Schema(description = "请求结果 false失败 true成功")
    private Boolean successFlag;
}
