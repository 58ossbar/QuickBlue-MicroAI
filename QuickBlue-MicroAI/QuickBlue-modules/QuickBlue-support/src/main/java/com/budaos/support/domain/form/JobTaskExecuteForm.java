package com.budaos.support.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 定时任务-手动执行
 *
 */
@Data
public class JobTaskExecuteForm {

    @Schema(description = "任务id")
    @NotNull(message = "任务id不能为空")
    private Integer jobId;

    @Schema(description = "定时任务参数|可选")
    @Length(max = 2000, message = "定时任务参数最多2000字符")
    private String param;

    @Schema(hidden = true)
    private String updateName;
}
