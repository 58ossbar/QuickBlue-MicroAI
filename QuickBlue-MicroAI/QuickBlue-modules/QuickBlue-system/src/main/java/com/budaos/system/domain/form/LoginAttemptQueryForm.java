package com.budaos.system.domain.form;

import com.budaos.common.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 登录失败查询表单
 *
 * @author budaos
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "登录失败查询表单")
public class LoginAttemptQueryForm extends PageQuery {

    @Schema(description = "登录名")
    private String loginName;

    @Schema(description = "锁定状态")
    private Boolean lockFlag;

    @Schema(description = "登录失败锁定时间开始")
    private LocalDate loginLockBeginTimeBegin;

    @Schema(description = "登录失败锁定时间结束")
    private LocalDate loginLockBeginTimeEnd;
}
