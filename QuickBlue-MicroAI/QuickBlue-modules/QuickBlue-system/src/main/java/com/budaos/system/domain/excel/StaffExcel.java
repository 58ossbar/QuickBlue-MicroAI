package com.budaos.system.domain.excel;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.annotation.write.style.HeadFontStyle;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;
import com.alibaba.excel.enums.poi.VerticalAlignmentEnum;
import com.alibaba.excel.enums.poi.BorderStyleEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 员工信息Excel导入导出实体
 *
 * 导入说明：
 * 1. 必填字段：登录账号、真实姓名、手机号码
 * 2. 性别填写：男 或 女
 * 3. 是否禁用填写：是 或 否
 * 4. 部门名称、岗位名称需填写系统中已存在的名称
 * 5. 导入后系统会自动生成8位随机密码
 *
 * @author budaos
 */
@Data
@ExcelIgnoreUnannotated
@HeadRowHeight(20)
@ContentRowHeight(18)
@HeadStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER, verticalAlignment = VerticalAlignmentEnum.CENTER,
            borderLeft = BorderStyleEnum.THIN, borderRight = BorderStyleEnum.THIN,
            borderTop = BorderStyleEnum.THIN, borderBottom = BorderStyleEnum.THIN)
@HeadFontStyle(fontHeightInPoints = 11)
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.LEFT, verticalAlignment = VerticalAlignmentEnum.CENTER,
              borderLeft = BorderStyleEnum.THIN, borderRight = BorderStyleEnum.THIN,
              borderTop = BorderStyleEnum.THIN, borderBottom = BorderStyleEnum.THIN)
@Schema(description = "员工信息Excel导入导出实体")
public class StaffExcel {

    @ExcelProperty(value = "登录账号*", index = 0)
    @ColumnWidth(20)
    @Schema(description = "登录账号，必填")
    private String loginName;

    @ExcelProperty(value = "真实姓名*", index = 1)
    @ColumnWidth(15)
    @Schema(description = "真实姓名，必填")
    private String actualName;

    @ExcelProperty(value = "性别", index = 2)
    @ColumnWidth(10)
    @Schema(description = "性别，填写：男 或 女")
    private String gender;

    @ExcelProperty(value = "手机号码*", index = 3)
    @ColumnWidth(15)
    @Schema(description = "手机号码，必填，11位数字")
    private String phone;

    @ExcelProperty(value = "邮箱", index = 4)
    @ColumnWidth(25)
    @Schema(description = "邮箱，选填")
    private String email;

    @ExcelProperty(value = "部门名称*", index = 5)
    @ColumnWidth(20)
    @Schema(description = "部门名称，必填，需填写系统中已存在的部门名称")
    private String departmentName;

    @ExcelProperty(value = "岗位名称", index = 6)
    @ColumnWidth(20)
    @Schema(description = "岗位名称，选填，需填写系统中已存在的岗位名称")
    private String positionName;

    @ExcelProperty(value = "是否禁用", index = 7)
    @ColumnWidth(12)
    @Schema(description = "是否禁用，填写：是 或 否，默认否")
    private String disabledFlag;

    @ExcelProperty(value = "备注", index = 8)
    @ColumnWidth(30)
    @Schema(description = "备注，选填")
    private String remark;
}
