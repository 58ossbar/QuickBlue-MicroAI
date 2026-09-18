package com.budaos.support.domain.form;

import com.budaos.common.core.domain.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 意见反馈查询
 *
 * @author budaos
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SuggestionQueryForm extends PageQuery {

    @Schema(description = "搜索词")
    @Size(max = 25, message = "搜索词最多25字符")
    private String searchWord;

    @Schema(description = "开始时间", example = "2021-02-14")
    private LocalDate startDate;

    @Schema(description = "截止时间", example = "2022-10-15")
    private LocalDate endDate;
}
