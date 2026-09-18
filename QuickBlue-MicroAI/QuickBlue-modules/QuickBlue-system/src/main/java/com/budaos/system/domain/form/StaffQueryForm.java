package com.budaos.system.domain.form;

import com.budaos.common.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.List;

/**
 * 员工查询表单
 *
 * @author budaos
 */
@Data
@Schema(description = "员工查询表单")
public class StaffQueryForm extends PageQuery {

    @Schema(description = "搜索词（登录名/姓名/手机号）")
    @Length(max = 20, message = "搜索词最多20字符")
    private String keyword;

    @Schema(description = "部门ID")
    private Long departmentId;

    @Schema(description = "是否包含子部门，默认false")
    private Boolean includeSubDepartment = false;

    @Schema(description = "是否禁用")
    private Boolean disabledFlag;

    @Schema(description = "员工ID集合")
    @Size(max = 99, message = "最多查询99个员工")
    private List<Long> employeeIdList;

    @Schema(description = "删除标识", hidden = true)
    private Boolean deletedFlag;
}
