package com.budaos.gateway.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 安装进度VO
 *
 * @author budaos
 */
@Data
@Schema(description = "安装进度")
public class InstallProgressVO {

    @Schema(description = "总体进度(0-100)")
    private Integer overallProgress;

    @Schema(description = "安装状态: pending/running/completed/error")
    private String status;

    @Schema(description = "任务列表")
    private List<InstallTask> tasks;

    @Data
    @Schema(description = "安装任务")
    public static class InstallTask {
        @Schema(description = "任务标题")
        private String title;

        @Schema(description = "任务状态: pending/running/completed/error")
        private String status;

        @Schema(description = "进度(0-100)")
        private Integer progress;

        @Schema(description = "错误信息")
        private String error;

        @Schema(description = "消息")
        private String message;
    }
}
