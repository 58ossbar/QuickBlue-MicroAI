package com.budaos.support.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 意见反馈添加表单
 *
 * @author budaos
 */
@Data
public class SuggestionAddForm {

    @Schema(description = "反馈内容")
    @NotBlank(message = "反馈内容不能为空")
    private String feedbackContent;

    @Schema(description = "反馈图片")
    private String feedbackAttachment;

}
