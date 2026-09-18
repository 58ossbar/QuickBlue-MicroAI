package com.budaos.system.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 在线用户查询表单
 *
 * @author budaos
 */
@Data
@Schema(description = "在线用户查询表单")
public class OnlineUserQueryForm {

    @Schema(description = "关键词(用户名/登录名)")
    private String keywords;

    @Schema(description = "部门ID")
    private Long departmentId;

    @Schema(description = "用户类型")
    private Integer userType;

    @Schema(description = "页码")
    private Integer pageNum;

    @Schema(description = "每页数量")
    private Integer pageSize;
}
