package com.budaos.support.core;

/**
 * 心跳处理接口
 *
 * @author budaos
 */
public interface IKeepAliveRecordHandler {

    /**
     * 心跳日志处理方法
     *
     * @param heartBeatRecord 心跳记录
     */
    void handler(KeepAliveRecord heartBeatRecord);
}
