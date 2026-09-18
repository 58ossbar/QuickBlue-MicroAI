package com.budaos.support.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 心跳记录 VO
 *
 * @author budaos
 */
@Data
@Schema(description = "心跳记录VO")
public class KeepAliveRecordVO {

    @Schema(description = "心跳记录ID")
    private Integer heartBeatRecordId;

    @Schema(description = "项目路径")
    private String projectPath;

    @Schema(description = "服务器ip")
    private String serverIp;

    @Schema(description = "进程号")
    private Integer processNo;

    @Schema(description = "进程开启时间")
    private Date processStartTime;

    @Schema(description = "心跳当前时间")
    private Date heartBeatTime;
}
