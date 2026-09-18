package com.budaos.business.notice.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 通知公告 更新表单
 *

 */
@Data
public class AnnouncementUpdateForm extends AnnouncementAddForm {

    @Schema(description = "id")
    @NotNull(message = "通知id不能为空")
    private Long noticeId;

}
