package com.budaos.gateway.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 环境检查VO
 *
 * @author budaos
 */
@Data
@Schema(description = "环境检查结果")
public class EnvironmentCheckVO {

    @Schema(description = "是否通过")
    private boolean passed;

    @Schema(description = "检查项列表")
    private List<CheckItem> items;

    @Data
    @Schema(description = "检查项")
    public static class CheckItem {
        @Schema(description = "名称")
        private String name;

        @Schema(description = "状态: success/warning/error")
        private String status;

        @Schema(description = "状态文本")
        private String statusText;

        @Schema(description = "消息")
        private String message;
    }
}
