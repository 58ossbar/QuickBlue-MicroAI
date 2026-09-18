package com.budaos.support.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 心跳记录实体
 *
 * @author budaos
 */
@Data
@TableName(value = "t_heart_beat_record")
public class KeepAliveRecordEntity implements Serializable {

    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Long heartBeatRecordId;

    /**
     * 项目名字
     */
    private String projectPath;

    /**
     * 服务器ip
     */
    private String serverIp;

    /**
     * 进程号
     */
    private Integer processNo;

    /**
     * 进程开启时间
     */
    private LocalDateTime processStartTime;

    /**
     * 心跳当前时间
     */
    private LocalDateTime heartBeatTime;
}
