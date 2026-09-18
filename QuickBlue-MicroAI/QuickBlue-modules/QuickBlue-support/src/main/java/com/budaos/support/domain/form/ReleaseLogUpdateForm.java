package com.budaos.support.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 系统更新日志更新表单
 *
 * @author budaos
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "系统更新日志更新表单")
public class ReleaseLogUpdateForm {

    @Schema(description = "更新日志ID")
    @NotNull(message = "更新日志ID不能为空")
    private Long changeLogId;

    @Schema(description = "版本")
    @NotBlank(message = "版本不能为空")
    private String updateVersion;

    @Schema(description = "更新类型:[1:特大版本功能更新;2:功能更新;3:bug修复]")
    @NotNull(message = "更新类型不能为空")
    private Integer type;

    @Schema(description = "发布人")
    @NotBlank(message = "发布人不能为空")
    private String publishAuthor;

    @Schema(description = "发布日期")
    @NotNull(message = "发布日期不能为空")
    private LocalDate publicDate;

    @Schema(description = "更新内容")
    @NotBlank(message = "更新内容不能为空")
    private String content;

    @Schema(description = "跳转链接")
    private String link;
}
