package com.budaos.support.domain.form;

import com.budaos.support.constant.AttachmentFolderTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * URL上传文件表单
 */
@Data
@Schema(description = "URL上传文件表单")
public class AttachmentUrlUploadForm {

    @Schema(description = "业务类型")
    @NotNull(message = "业务类型不能为空")
    private Integer folder;

    @Schema(description = "文件URL")
    @NotBlank(message = "文件URL不能为空")
    private String fileUrl;

}
